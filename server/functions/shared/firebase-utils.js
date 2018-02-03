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
      firestoreUtils.deleteCollection(colRef)

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

  // ----------------------------- Promise based functions  ------------------------------------------ //

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
    // Get all existing queues
    firestoreUtils.getCollection(colRef).then((queues) => {
       // Get all historical bookings
      const historyColRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId)
                                .collection(constants.QueueCollection).limit(BATCH_SIZE)
      const archiveSummary = new ArchiveSummary(0, 0, 0)
      return new Promise((resolve, reject) => {
        batchArchiveHistory(archiveSummary, entityId, queues, firebaseAdmin.firestore(), historyColRef, BATCH_SIZE, resolve, reject)
      })
    }).catch((err) => {
      console.error(err)
    })
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
        console.log('COMPLETE ARCHIVING *****')
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
   * Get lookup data.
   *
   * @param {string} lookupType Optional lookup type.
   * @returns {Object} The lookup data.
   */
  self.getLookup = function (lookupType = null) {
    var docRef = null
    if (lookupType) {
      docRef = firebaseAdmin.firestore().collection(constants.LookupCollection).doc(lookupType)
      return firestoreUtils.getDocument(docRef)
    } else {
      docRef = firebaseAdmin.firestore().collection(constants.LookupCollection)
      return firestoreUtils.getCollection(docRef)
    }
  }

  /**
   * Get a particular entity or a list of entities.
   *
   * @param {string} entityId Optional entity id.
   * @returns {Object} Entity or list of entities.
   * @public
   */
  self.getEntities = function (entityId = null) {
    if (entityId != null) {
      // Get a particular entity
      const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId)
      return firestoreUtils.getDocument(docRef)
    } else {
      // Get all entities
      const colRef = firebaseAdmin.firestore().collection(constants.EntityCollection)
      return firestoreUtils.getCollection(colRef)
    }
  }

  /**
   * Get list of entities by email. By design it should return only 1.
   *
   * @param {string} email Email.
   * @returns {Object} List of entities.
   * @public
   */
  self.getEntitiesByEmail = function (email) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).where(Entity.EMAIL_FIELD, '==', email)
    return firestoreUtils.getDocumentByQuery(docRef)
  }

 /**
  * Save a entity.
  *
  * @param {Object} entity Entity to save.
  * @public
  */
  self.saveEntity = function (entity) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entity.id)
    return firestoreUtils.saveDocument(docRef, entity)
  }

  /**
   * Save a queue.
   *
   * @param {string} entityId Entity id.
   * @param {Object} queue Queue to save.
   * @public
   */
  self.saveQueue = function (entityId, queue) {
    const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queue.id)
    return firestoreUtils.saveDocument(docRef, queue)
  }

  /**
   * Delete a queue. The queue and history MUST be empty. Action cannot be recovered.
   *
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.deleteQueue = function (entityId, queueId) {
    const colRef = firebaseAdmin.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    const historyColRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).where(History.QUEUE_ID_FIELD, '==', queueId)
    return new Promise((resolve, reject) => {
      firestoreUtils.getCollectionCount(colRef).then((documentCount) => {
        if (documentCount === 0) {
          // Queue is empty, check history queue
          return firestoreUtils.getCollectionCount(historyColRef)
        } else {
          // Throw error - queue is not empty
          throw new ApplicationError(HttpStatus.METHOD_NOT_ALLOWED, constants.QueueNotEmpty, 'Path: {0}'.format(colRef.path))
        }
      }).then((historyCount) => {
        if (historyCount === 0) {
          // History queue is empty, proceed to delete
          const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
          return firestoreUtils.deleteDocument(docRef)
        } else {
          // Throw error - history queue is not empty
          throw new ApplicationError(HttpStatus.METHOD_NOT_ALLOWED, constants.HistoryNotEmpty)  // Path not availabel for Query
        }
      }).then(() => {
        resolve()
      }).catch((err) => {
        reject(err)
      })
    }).then((results) => {
      return results
    }).catch((err) => {
      throw err
    })
  }

  /**
   * Get a particular queue or list of queues.
   *
   * @param {string} entityId Entity id.
   * @param {string} queueId Optional queue id.
   * @returns {Object} Queue or list of queues.
   * @public
   */
  self.getQueues = function (entityId, queueId = null) {
    if (queueId != null) {
      const docRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
      return firestoreUtils.getDocument(docRef)
    } else {
      const colRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
      return firestoreUtils.getCollection(colRef)
    }
  }

  /**
   * Get archive summaries for an entity.
   *
   * @param {string} entityId Entity id.
   * @returns {Object} List of archives.
   * @public
   */
  self.getArchives = function (entityId) {
    const colRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId).collection(constants.QueueCollection).orderBy(ArchiveSummary.FROM_DATE_FIELD, 'desc')
    return firestoreUtils.getCollection(colRef)
  }

  /**
   * Get particular archive details for an entity.
   *
   * @param {string} entityId Entity id.
   * @param {string} archiveId Archive id.
   * @returns {Object} List of archives.
   * @public
   */
  self.getArchive = function (entityId, archiveId) {
    const colRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId)
                      .collection(constants.QueueCollection).doc(archiveId).collection(constants.HistoryCollection).orderBy(History.HISTORY_DATE_FIELD, 'desc')
    return firestoreUtils.getCollection(colRef)
  }

 /**
  * Delete archives for an entity.
  *
  * @param {string} entityId Entity id.
  * @param {string} archiveId List of comma separated archive ids.
  * @public
  */
  self.deleteArchives = function (entityId, archiveIds) {
    const idList = archiveIds.split(',')
    return new Promise((resolve, reject) => {
      idList.forEach(function (id) {
        // Delete archive summary
        const docRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId).collection(constants.QueueCollection).doc(id)
        firestoreUtils.deleteDocument(docRef).then(() => {
          // Delete archived history
          const colRef = firebaseAdmin.firestore().collection(constants.ArchiveCollection).doc(entityId).collection(constants.QueueCollection).doc(id).collection(constants.HistoryCollection)
          firestoreUtils.deleteCollection(colRef)
        })
      })
      resolve()
    }).then(() => {
    }).catch((err) => {
      throw err
    })
  }

  /**
   * Delete all archives for an entity.
   *
   * @param {string} entityId Entity id.
   * @public
   */
  self.deleteAllArchives = function (entityId) {
      // TODO
  }

  /**
   * Get historical bookings for a particular entity.
   *
   * @param {string} entityId Entity id.
   * @returns {Object} List of historical bookings.
   * @public
   */
  self.getHistories = function (entityId) {
    const colRef = firebaseAdmin.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection)
        .orderBy(History.HISTORY_DATE_FIELD, 'desc')
    return firestoreUtils.getCollection(colRef)
  }

  /**
   * Return a particular booking from history.
   *
   * @param {string} action Return the booking.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {string} bookingId Booking id.
   * @public
   */
  self.returnHistory = function (action, entityId, queueId, bookingId) {
    // Check if the queue exists
    const queueDocRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    return new Promise((resolve, reject) => {
      firestoreUtils.getDocument(queueDocRef).then((queueDoc) => {
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
          resolve(results)
        }).catch(err => {
          reject(err)
        })
      }).catch((err) => {
        reject(err)
      })
    }).then((results) => {
      return results
    }).catch((err) => {
      throw err
    })
  }

  /**
   * Archive the historical bookings for an entity.
   *
   * @param {string} entityId Entity id.
   * @public
   */
  self.archiveHistory = function (entityId) {
    return new Promise((resolve, reject) => {
      // Get all active queues, and archive all their histories
      const colRef = firebaseAdmin.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
      archiveQueues(colRef, entityId)
      resolve()
    }).then(() => {
    }).catch((err) => {
      throw err
    })
  }

  return self
})()

module.exports = {
  fireStore: FireStore,
  auth: Auth,
  storage: Storage
}
