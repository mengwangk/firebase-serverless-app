'use strict'

const firebase = require('firebase-admin')
const firestoreUtils = require('./firestore-utils')
const constants = require('./constants')
const utils = require('./utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Entity = require('../models/entity')
const History = require('../models/history')
const Booking = require('../models/booking')
const Archive = require('../models/archive')

/**
 * Firebase helper class.
 * @public
 */
const FireStore = (function () {
  var self = {}

  /**
   * Send all the bookings to history.
   *
   * @param {Object} colRef Collection reference.
   * @param {number} batchSize Delete batch size.
   * @private
   */
  const sendToHistory = function (colRef, entityId, queueId, batchSize = 100) {
    const query = colRef.limit(batchSize)
    return new Promise((resolve, reject) => {
      batchUpdateBookingStatus(entityId, queueId, constants.BookingAction.remove, firebase.firestore(), query, batchSize, resolve, reject)
    })
  }

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
  const sendToArchive = function (colRef, entityId, batchSize = 100) {
    const query = colRef.limit(batchSize)
    return new Promise((resolve, reject) => {
      batchUpdateBookingStatus(entityId, '', constants.HistoryAction.archive, firebase.firestore(), query, batchSize, resolve, reject)
    })
  }

  /**
   * Creat a history booking object.
   *
   * @param {string} entityId Entity id.
   * @param {Object} doc Document snapshot.
   * @param {string} status "Archived".
   */
  const createArchiveBooking = function (entityId, doc, status) {
    const history = doc.data()
    const archive = new Archive(status, history)
    var archiveData = JSON.stringify(archive)
    return JSON.parse(archiveData)
  }

  /**
   * Delete the bookings and send to history.
   *
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {string} action Batch remove or archive the bookings.
   * @param {Object} db Firestore database.
   * @param {Object} query Query.
   * @param {number} batchSize Batch size.
   * @param {function} resolve Success promise.
   * @param {function} reject Failure promise.
   * @private
   */
  const batchUpdateBookingStatus = function (entityId, queueId, action, db, query, batchSize, resolve, reject) {
    query.get().then((snapshot) => {
      // When there are no documents left, we are done
      if (snapshot.size === 0) {
        return 0
      }
      // Delete documents in a batch
      var batch = db.batch()
      snapshot.docs.forEach((doc) => {
        if (action === constants.BookingAction.remove) {
          // Send to history
          const booking = doc.data()
          const historyBooking = createHistoryBooking(entityId, queueId, doc, constants.BookingStatus.removed)
          const historyDocRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).doc(booking.id)
          batch.set(historyDocRef, historyBooking)
        } else if (action === constants.HistoryAction.archive) {
          // Send to archive
          const history = doc.data()
          const archiveBooking = createArchiveBooking(entityId, doc, constants.BookingStatus.archived)
          const archiveDocRef = firebase.firestore().collection(constants.ArchiveCollection).doc(entityId).collection(constants.QueueCollection).doc(history.id)
          batch.set(archiveDocRef, archiveBooking)
        }
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
        batchUpdateBookingStatus(entityId, queueId, action, db, query, batchSize, resolve, reject)
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
    const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entity.id)
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
    const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queue.id)
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
    const queueDocRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    const bookingDocRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(booking.id)
    firebase.firestore().runTransaction(t => {
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
      docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      // Get all entities
      docRef = firebase.firestore().collection(constants.EntityCollection)
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
    const docRef = firebase.firestore().collection(constants.EntityCollection).where(Entity.EMAIL_FIELD, '==', email)
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
      docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
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
    const docRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    firestoreUtils.getCol(docRef, callback)
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
    const docRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    firestoreUtils.getColCount(docRef, callback)
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
    const bookingDocRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(bookingId)
    const historyDocRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).doc(bookingId)
    firebase.firestore().runTransaction(t => {
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
   *
   * @param {function} callback Call back function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.clearQueue = function (callback, entityId, queueId) {
    try {
      // Delete the queue collection
      const colRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
      sendToHistory(colRef, entityId, queueId)

      // Reset the queue counter
      const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
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
   * Delete a queue.
   *
   * @param {function} callback Callback function.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @public
   */
  self.deleteQueue = function (callback, entityId, queueId) {
    try {
      // Clear the queue
      const colRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
      sendToHistory(colRef, entityId, queueId)

      // Delete the queue
      const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
      firestoreUtils.deleteDoc(docRef, callback)
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
      docRef = firebase.firestore().collection(constants.LookupCollection).doc(lookupType)
      firestoreUtils.getDoc(docRef, callback)
    } else {
      docRef = firebase.firestore().collection(constants.LookupCollection)
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
    const colRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection)
    // .orderBy(Booking.BOOKED_DATE_FIELD ,'desc')
    firestoreUtils.getCol(colRef, callback)
  }

  /**
   * Return a particular booking from history.
   *
   * @param {function} callback Call back function.
   * @param {string} action Return or archive the booking.
   * @param {string} entityId Entity id.
   * @param {string} queueId Queue id.
   * @param {string} bookingId Booking id.
   * @public
   */
  self.deleteHistory = function (callback, action, entityId, queueId, bookingId) {
    let transactionHandler = (results = '', err = null) => {
      if (err != null) {
        callback(results, err)
      } else {
        // Proceed to return or achive the booking
        const bookingDocRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(bookingId)
        const historyDocRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).doc(bookingId)
        firebase.firestore().runTransaction(t => {
          return t.get(historyDocRef).then(historyDoc => {
            if (historyDoc.exists) {
              const history = historyDoc.data()
              if (action === constants.HistoryAction.return) {
                // Return to the original queue
                const booking = new Booking(history.name, history.contactNo, history.noOfSeats, history.id, history.bookingNo, history.bookedDate)
                const bookingData = JSON.stringify(booking)
                t.set(bookingDocRef, JSON.parse(bookingData))
              } else {
                // TODO archive the history - not required now for individual booking
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
    const queueDocRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    firestoreUtils.getDoc(queueDocRef, transactionHandler)
  }

  /**
   * Archive the historical bookings.
   *
   * @param {function} callback Callback function.
   * @param {string} action "archive".
   * @param {string} entityId Entity id.
   * @public
   */
  self.archiveHistory = function (callback, action, entityId) {
    try {
      // Archive the historical queue
      const colRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection)
      sendToArchive(colRef, entityId)
      callback()
    } catch (err) {
      callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    }
  }

  return self
})()

module.exports = {
  fireStore: FireStore
}
