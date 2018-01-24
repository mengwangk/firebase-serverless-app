'use strict'

const firebase = require('firebase-admin')
const constants = require('./constants')
const utils = require('./utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Entity = require('../models/entity')
const History = require('../models/history')
const Booking = require('../models/booking')

/**
 * Firestore helper class.
 * @public
 */
const FireStore = (function () {
  var self = {}

  /**
   * Save a document.
   *
   * @param {Object} docRef Document reference.
   * @param {Object} obj Object to save.
   * @param {function} callback Call back function.
   * @returns {Object} Saved object.
   * @private
   */
  const saveDoc = function (docRef, obj, callback = null) {
    const docData = JSON.stringify(obj)
    if (callback) {
      docRef.set(JSON.parse(docData), { merge: true }).then(() => {
        callback(obj)
      }).catch((err) => {
        callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
      })
    } else {
      docRef.set(JSON.parse(docData), { merge: true })
      return obj
    }
  }

  /**
   * Delete a document.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @private
   */
  const deleteDoc = function (docRef, callback = null) {
    if (callback) {
      docRef.delete().then(() => {
        callback()
      }).catch((err) => {
        callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
      })
    } else {
      docRef.delete()
    }
  }

  /**
   * Delete a collection.
   *
   * @param {Object} colRef Collection reference.
   * @param {number} batchSize Delete batch size
   * @private
   */
  const deleteCol = function (colRef, batchSize = 100) {
    const query = colRef.limit(batchSize)
    return new Promise((resolve, reject) => {
      deleteBatch(firebase.firestore(), query, batchSize, resolve, reject)
    })
  }

  /**
   * Delete documents in batch.
   *
   * @param {Object} db Firestore database.
   * @param {Object} query Query.
   * @param {number} batchSize Batch size.
   * @param {function} resolve Success promise.
   * @param {function} reject Failure promise.
   * @private
   */
  const deleteBatch = function (db, query, batchSize, resolve, reject) {
    query.get()
            .then((snapshot) => {
              // When there are no documents left, we are done
              if (snapshot.size === 0) {
                return 0
              }

              // Delete documents in a batch
              var batch = db.batch()
              snapshot.docs.forEach((doc) => {
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
                deleteBatch(db, query, batchSize, resolve, reject)
              })
            })
            .catch(reject)
  }

  /**
   * Get a collection of objects.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @returns {Object} List of objects.
   * @private
   */
  const getCol = function (docRef, callback) {
    docRef.get()
            .then((snapshot) => {
              var docList = []
              snapshot.forEach((doc) => {
                docList.push(doc.data())
              })
              callback(docList)
            })
            .catch((err) => {
              callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
            })
  }

  /**
   * Get a document.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @returns {Object} Found document.
   * @private
   */
  const getDoc = function (docRef, callback) {
    docRef.get()
            .then((doc) => {
              if (!doc.exists) {
                callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(docRef.path)))
              } else {
                callback(doc.data())
              }
            })
            .catch((err) => {
              callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
            })
  }

  /**
   * Get a list of objects from a query document reference.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @returns {Object} List of found objects.
   * @private
   */
  const getDocByQuery = function (docRef, callback) {
    docRef.get()
            .then((snapshot) => {
              if (snapshot.size <= 0) {
                callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound))
                /*
                } else if (snapshot.size == 1) {
                    var doc = snapshot.docs[0];
                    callback(doc.data());
                */
              } else {
                var docList = []
                snapshot.forEach((doc) => {
                  docList.push(doc.data())
                })
                callback(docList)
              }
            })
            .catch((err) => {
              callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
            })
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
    saveDoc(docRef, entity, callback)
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
    saveDoc(docRef, queue, callback)
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
      return t.get(queueDocRef)
                .then(doc => {
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
                    return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound,
                            'Path: {0}'.format(queueDocRef.path)))
                  }
                })
    })
        .then(results => {
          callback(results)
        })
        .catch(err => {
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
      getDoc(docRef, callback)
    } else {
      // Get all entities
      docRef = firebase.firestore().collection(constants.EntityCollection)
      getCol(docRef, callback)
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
    getDocByQuery(docRef, callback)
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
      getDoc(docRef, callback)
    } else {
      docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection)
      getCol(docRef, callback)
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
    getCol(docRef, callback)
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
      return t.get(bookingDocRef)
                .then(doc => {
                  if (doc.exists) {
                    // Save to history
                    const booking = doc.data()
                    const status = (action === constants.BookingAction.done ? constants.BookingStatus.done : constants.BookingStatus.removed)
                    const history = new History(queueId, status, booking)
                    var historyData = JSON.stringify(history)
                    t.set(historyDocRef, JSON.parse(historyData))

                    // Remove the booking
                    t.delete(bookingDocRef)
                    return Promise.resolve(booking)
                  } else {
                    return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound,
                            'Path: {0}'.format(bookingDocRef.path)))
                  }
                })
    })
        .then(results => {
          callback(results)
        })
        .catch(err => {
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
    // Delete the queue collection
    const colRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    deleteCol(colRef)

    // Reset the queue counter
    const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    docRef.update({ counter: 0 }).then(() => {
      callback()
    }).catch((err) => {
      callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
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
    // Clear the queue
    const colRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId)
    deleteCol(colRef)

    // Delete the queue
    const docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    deleteDoc(docRef, callback)
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
      getDoc(docRef, callback)
    } else {
      docRef = firebase.firestore().collection(constants.LookupCollection)
      getCol(docRef, callback)
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
    const colRef = firebase.firestore().collection(constants.HistoryCollection).doc(entityId).collection(constants.QueueCollection).orderBy(Booking.BOOKED_DATE_FIELD ,'desc')
    getCol(colRef, callback)
  }

  /**
   * Restore a particular booking from history.
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
          return t.get(historyDocRef)
                .then(historyDoc => {
                  if (historyDoc.exists) {
                    const history = historyDoc.data()
                    if (action === constants.HistoryAction.return) {
                      // Return to the original queue
                      const booking = new Booking(history.name, history.contactNo, history.noOfSeats, history.id, history.bookingNo, history.bookedDate)
                      const bookingData = JSON.stringify(booking)
                      t.set(bookingDocRef, JSON.parse(bookingData))
                    } else {
                      // TODO archive the history
                    }
                    // Delete the history
                    t.delete(historyDocRef)
                    return Promise.resolve(history)
                  } else {
                    return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(historyDocRef.path)))
                  }
                })
        })
        .then(results => {
          callback(results)
        })
        .catch(err => {
          callback(null, err)
        })
      }
    }

    // Check if the queue exists
    const queueDocRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId)
    getDoc(queueDocRef, transactionHandler)
  }

  return self
})()

module.exports = {
  fireStore: FireStore
}
