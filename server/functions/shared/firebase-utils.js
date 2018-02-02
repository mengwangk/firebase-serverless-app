'use strict'

const firebaseAdmin = require('firebase-admin')
const firestoreUtils = require('./firestore-utils')
const constants = require('./constants')
const utils = require('./utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Entity = require('../models/entity')
const History = require('../models/history')
const Booking = require('../models/booking')
const ArchiveSummary = require('../models/archive-summary')
const Archive = require('../models/archive')

/**
 * Auth helper class.
 * @public
 */
const Auth = (function () {
  var self = {}

   /**
   * Create a firebase user.
   * Refer to https://stackoverflow.com/questions/47268411/create-users-server-side-firebase-functions
   *
   * @param {string} email Email.
   * @param {string} password Password.
   */
  self.createUser = function (email, password) {
    return firebaseAdmin.auth().createUser(
      {
        email: email,
        password: password
      }
    )
  }

  return self
})()

/**
 * Cloud storage helper class.
 * @public
 */
const Storage = (function () {
  var self = {}

  /**
   * Upload a file.
   * https://cloud.google.com/nodejs/docs/reference/storage/1.5.x/Bucket
   *
   * @param {Object} entity Entity.
   * @param {Object} file File object.
   */
  self.upload = function (entity, file) {
    var bucket = firebaseAdmin.storage().bucket()
    var options = {
      destination: entity.avatar,
      resumable: false
    }
    return bucket.upload(file.path, options)
  }

  return self
})()

/**
 * Firestore helper class.
 * @public
 */
const FireStore = (function () {
  var self = {}

  /**
   * Creat a history booking object.
   *
   * @param {string} entityId Entity id.
   * @param {string} queueId  Queue id.
   * @param {Object} doc Document snapshot.
   * @param {string} status Removed or Done.
   */
  const createHistoryBooking = function (entityId, queueId, doc, status) {
    const booking = doc.data()
    const history = new History(queueId, status, booking)
    var historyData = JSON.stringify(history)
    return JSON.parse(historyData)
  }

  /**
   * Send all the historical bookings to archive.
   *
   * @param {Object} colRef Collection reference.
   * @param {string} entityId Entity id.
   * @param {number} batchSize Delete batch size.
   * @private
   */
  const archiveQueues = function (colRef, entityId) {
    const BATCH_SIZE = constants.TransactionBatchSize
    var queues = []
    let callback = (results = '', err = null) => {
      if (!err) {
        queues = results
        // Get all historical bookings
        const historyColRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).limit(BATCH_SIZE)
        const archiveSummary = new ArchiveSummary(0, 0, 0)

        return new Promise((resolve, reject) => {
          batchArchiveHistory(archiveSummary, entityId, queues, firebaseAdmin.firestore(), historyColRef, BATCH_SIZE, resolve, reject)
        })
      } else {
        console.error(err)
      }
    }
       // Get all existing queues
    firestoreUtils.getCol(colRef, callback)
  }

  /**
   * Create a history booking object.
   *
   * @param {string} entityId Entity id.
   * @param {Object} queues Queues lookup.
   * @param {Object} doc Document snapshot.
   * @param {string} status "Archived".
   * @param {string} queueName Queue name
   */
  const createArchiveBooking = function (entityId, queues, doc, status, queueName) {
    const history = doc.data()
    const queue = queues.filter(queue => queue.id === history.queueId)
    const archive = new Archive(status, history, queue[0].name)
    var archiveData = JSON.stringify(archive)
    return JSON.parse(archiveData)
  }

  /**
   * Delete the bookings and send to history.
   *
   * @param {archiveSummary} Archive summmary object.
   * @param {string} entityId Entity id.
   * @param {Object} queues List of queues.
   * @param {Object} db Firestore database.
   * @param {Object} query Query.
   * @param {number} batchSize Batch size.
   * @param {function} resolve Success promise.
   * @param {function} reject Failure promise.
   * @private
   */
  const batchArchiveHistory = function (archiveSummary, entityId, queues, db, query, batchSize, resolve, reject) {
    query.get().then((snapshot) => {
      // When there are no documents left, we are done
      if (snapshot.size === 0) {
        return 0
      }
      // Derive the summary for each batch
      archiveSummary.totalBookings += snapshot.size
      const maxDate = Math.max.apply(Math, snapshot.docs.map(function (o) { return o.data().bookedDate }))
      const minDate = Math.min.apply(Math, snapshot.docs.map(function (o) { return o.data().bookedDate }))
      if (maxDate > archiveSummary.toDate || archiveSummary.toDate === 0) archiveSummary.toDate = maxDate
      if (minDate < archiveSummary.fromDate || archiveSummary.fromDate === 0) archiveSummary.fromDate = minDate

      const batch = db.batch()
      const archiveSummaryDocRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId)
                                    .collection(constants.QueueCollection).doc(archiveSummary.id)
      batch.set(archiveSummaryDocRef, JSON.parse(JSON.stringify(archiveSummary)))
      snapshot.docs.forEach((doc) => {
        // Send to archive
        const history = doc.data()
        const archiveBooking = createArchiveBooking(entityId, queues, doc, constants.BookingStatus.archived)
        const historyDocDocRef = archiveSummaryDocRef.collection(constants.HistoryCollection).doc(history.id)
        batch.set(historyDocDocRef, archiveBooking)
        batch.delete(doc.ref)
      })

      return batch.commit().then(() => {
        return snapshot.size
      })
    }).then((numDeleted) => {
      if (numDeleted === 0) {
        resolve()
        return
      }

      // Recurse on the next process tick, to avoid exploding the stack.
      process.nextTick(() => {
        batchArchiveHistory(archiveSummary, entityId, queues, db, query, batchSize, resolve, reject)
      })
    }).catch(reject)
  }

  /**
   * Save a entity.
   *
   * @param {function} callback Call back function.
   * @param {Object} entity Entity to save.
   * @public
   */
  self.saveEntity = function (callback, entity) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entity.id)
    firestoreUtils.saveDoc(docRef, entity, callback)
  }

  /**
   * Save a queue.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {Object} queue Queue to save.
   * @public
   */
  self.saveQueue = function (callback, entityId, queue) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queue.id)
    firestoreUtils.saveDoc(docRef, queue, callback)
  }

  /**
   * Save a booking.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {Object} booking Booking object.
   * @public
   */
  self.saveBooking = function (callback, entityId, queueId, booking) {
    const queueDocRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    const bookingDocRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(booking.id)
    firebaseAdmin.firestore().runTransaction(t => {
      return t.get(queueDocRef).then(doc => {
        if (doc.exists) {
          if (!booking.bookingNo) {
            // Set the booking no
            var queue = doc.data()
            var newCounter = queue.counter + 1
            booking.bookingNo = queue.prefix + newCounter.pad(3)
            t.update(queueDocRef, { counter: utils.Counter.next(newCounter) })
          }
          var bookingData = JSON.stringify(booking)
          t.set(bookingDocRef, JSON.parse(bookingData))
          return Promise.resolve(booking)
        } else {
          return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(queueDocRef.path)))
        }
      })
    }).then(results => {
      callback(results)
    }).catch(err => {
      callback(null, err)
    })
  }

  /**
   * Get a particular entity or a list of entities.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Optional entity id.
   * @returns {Object} Entity or list of entities.
   * @public
   */
  self.getEntities = function (callback, entityId = null) {
    var docRef = null
    if (entityId != null) {
      // Get a particular entity
      docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      // Get all entities
      docRef = firebaseAdmin.firestore().collection(constants.EntityCollection)
      firestoreUtils.getCol(docRef, callback)
    }
  }

  /**
   * Get list of entities by email. By design it should return only 1.
   *
   * @param {function} callback Call back function.
   * @param {string} email Email.
   * @returns {Object} List of entities.
   * @public
   */
  self.getEntitiesByEmail = function (callback, email) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).where(Entity.EMAIL_FIELD, '==', email)
    firestoreUtils.getDocByQuery(docRef, callback)
  }

  /**
   * Get a particular queue or list of queues.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Optional queue id.
   * @returns {Object} Queue or list of queues.
   * @public
   */
  self.getQueues = function (callback, entityId, queueId = null) {
    var docRef = null
    if (queueId != null) {
      docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
      firestoreUtils.getCol(docRef, callback)
    }
  }

  /**
   * Get a list of bookings under a queue.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @returns {Object} List of bookings.
   * @public
   */
  self.getBookings = function (callback, entityId, queueId) {
    const colRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    firestoreUtils.getCol(colRef, callback)
  }

  /**
   * Get the total booking count under a queue.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.getBookingsCount = function (callback, entityId, queueId) {
    const colRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    firestoreUtils.getColCount(colRef, callback)
  }

  /**
   * Delete a booking.
   *
   * @param {function} callback Call back function.
   * @param {string} action Remove or done with the booking.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {string} bookingId Booking id.
   * @public
   */
  self.deleteBooking = function (callback, action, entityId, queueId, bookingId) {
    const bookingDocRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(bookingId)
    const historyDocRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).doc(bookingId)
    firebaseAdmin.firestore().runTransaction(t => {
      return t.get(bookingDocRef).then(doc => {
        if (doc.exists) {
          // Save to history
          const booking = doc.data()
          const status = (action === constants.BookingAction.done ? constants.BookingStatus.done : constants.BookingStatus.removed)
          const historyBooking = createHistoryBooking(entityId, queueId, doc, status)
          t.set(historyDocRef, historyBooking)

          // Remove the booking
          t.delete(bookingDocRef)
          return Promise.resolve(booking)
        } else {
          return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(bookingDocRef.path)))
        }
      })
    }).then(results => {
      callback(results)
    }).catch(err => {
      callback(null, err)
    })
  }

  /**
   * Clear a queue and reset the counter.
   * The bookings will be permanently deleted (NOT in history or archive).
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.clearQueue = function (callback, entityId, queueId) {
    try {
      // Delete the queue collection
      const colRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
      firestoreUtils.deleteCol(colRef)

      // Reset the queue counter
      const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
      docRef.update({ counter: 0 }).then(() => {
        callback()
      }).catch((err) => {
        callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
      })
    } catch (err) {
      callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    }
  }

  /**
   * Delete a queue. The queue and history MUST be empty. Action cannot be recovered.
   *
   * @param {function} callback Callback function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.deleteQueue = function (callback, entityId, queueId) {
    try {
      const colRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
      const historyColRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).where(History.QUEUE_ID_FIELD, '==', queueId)

      let step1 = (results = '', err = null) => {
        if (err != null) {
          callback(results, err)
        } else {
          if (results === 0) {
            // Queue is empty, check history queue
            firestoreUtils.getColCount(historyColRef, step2)
          } else {
            // Throw error - queue is not empty
            callback(null, new ApplicationError(HttpStatus.METHOD_NOT_ALLOWED, constants.QueueNotEmpty, 'Path: {0}'.format(colRef.path)))
          }
        }
      }

      let step2 = (results = '', err = null) => {
        if (err != null) {
          callback(results, err)
        } else {
          if (results === 0) {
            // History queue is empty, proceed to delete
            const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
            firestoreUtils.deleteDoc(docRef, callback)
          } else {
            // Throw error - queue is not empty
            callback(null, new ApplicationError(HttpStatus.METHOD_NOT_ALLOWED, constants.HistoryNotEmpty, 'Path: {0}'.format(historyColRef.path)))
          }
        }
      }

      // Checking existing queue count
      firestoreUtils.getColCount(colRef, step1)
    } catch (err) {
      callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    }
  }

  /**
   * Get lookup data.
   *
   * @param {function} callback Callback function.
   * @param {string} lookupType Optional lookup type.
   * @returns {Object} The lookup data.
   */
  self.getLookup = function (callback, lookupType) {
    var docRef = null
    if (lookupType) {
      docRef = firebaseAdmin.firestore().collection(constants.LookupCollection).doc(lookupType)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      docRef = firebaseAdmin.firestore().collection(constants.LookupCollection)
      firestoreUtils.getCol(docRef, callback)
    }
  }

  /**
   * Get historical bookings for a particular entity.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @returns {Object} List of historical bookings.
   * @public
   */
  self.getHistories = function (callback, entityId) {
    const colRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection)
    // .orderBy(Booking.BOOKED_DATE_FIELD ,'desc')
    firestoreUtils.getCol(colRef, callback)
  }

  /**
   * Return a particular booking from history.
   *
   * @param {function} callback Call back function.
   * @param {string} action Return the booking.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {string} bookingId Booking id.
   * @public
   */
  self.returnHistory = function (callback, action, entityId, queueId, bookingId) {
    let transactionHandler = (results = '', err = null) => {
      if (err != null) {
        callback(results, err)
      } else {
        // Proceed to return or achive the booking
        const bookingDocRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(bookingId)
        const historyDocRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).doc(bookingId)
        firebaseAdmin.firestore().runTransaction(t => {
          return t.get(historyDocRef).then(historyDoc => {
            if (historyDoc.exists) {
              const history = historyDoc.data()
              if (action === constants.HistoryAction.return) {
                // Return to the original queue
                const booking = new Booking(history.name, history.contactNo, history.noOfSeats, history.id, history.bookingNo, history.bookedDate)
                const bookingData = JSON.stringify(booking)
                t.set(bookingDocRef, JSON.parse(bookingData))
              }
              // Delete the history
              t.delete(historyDocRef)
              return Promise.resolve(history)
            } else {
              return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(historyDocRef.path)))
            }
          })
        }).then(results => {
          callback(results)
        }).catch(err => {
          callback(null, err)
        })
      }
    }

    // Check if the queue exists
    const queueDocRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    firestoreUtils.getDoc(queueDocRef, transactionHandler)
  }

  /**
   * Archive the historical bookings for an entity.
   *
   * @param {function} callback Callback function.
   * @param {string} entityId Entity id.
   * @public
   */
  self.archiveHistory = function (callback, entityId) {
    try {
      // Get all active queues, and archive all their histories
      const colRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
      archiveQueues(colRef, entityId)
      callback()
    } catch (err) {
      callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    }
  }

   /**
   * Get archives for an entity.
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @returns {Object} List of archives.
   * @public
   */
  self.getArchives = function (callback, entityId, queueId) {
    const colRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId).collection(constants.QueueCollection).orderBy(ArchiveSummary.FROM_DATE_FIELD, 'desc')
    firestoreUtils.getCol(colRef, callback)
  }

  return self
})()

module.exports = {
  fireStore: FireStore,
  auth: Auth,
  storage: Storage
}
