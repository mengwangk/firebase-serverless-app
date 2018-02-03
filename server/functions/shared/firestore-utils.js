'use strict'

/**
 * Firestore helper class.
 * @public
 */
const firebase = require('firebase-admin')
const constants = require('./constants')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')

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
   * Get a list of objects from a query document reference.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @returns {Object} List of found objects.
   * @private
   */
const getDocByQuery = function (docRef, callback) {
  docRef.get().then((snapshot) => {
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
  }).catch((err) => {
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
  docRef.get().then((doc) => {
    if (!doc.exists) {
      callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(docRef.path)))
    } else {
      callback(doc.data())
    }
  }).catch((err) => {
    callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
  })
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
  docRef.get().then((snapshot) => {
    var docList = []
    snapshot.forEach((doc) => {
      docList.push(doc.data())
    })
    callback(docList)
  }).catch((err) => {
    callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
  })
}

/**
   * Get collection count.
   *
   * @param {Object} docRef Document reference.
   * @param {function} callback Call back function.
   * @returns {Object} List of objects.
   * @private
   */
const getColCount = function (docRef, callback) {
  docRef.get().then((snapshot) => {
    callback(snapshot.size)
  }).catch((err) => {
    callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
  })
}

// ----------------------------- Promise based functions  ------------------------------------------ //

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
  query.get().then((snapshot) => {
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
  }).catch(reject)
}

/**
 * Get a document.
 *
 * @param {Object} docRef Document reference.
 * @returns {Object} Found document.
 * @private
 */
const getDocument = function (docRef) {
  return new Promise((resolve, reject) => {
    docRef.get().then((doc) => {
      if (!doc.exists) {
        reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(docRef.path)))
      } else {
        resolve(doc.data())
      }
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then((results) => {
    return results
  }).catch((err) => {
    throw err
  })
}

/**
 * Get a collection of objects.
 *
 * @param {Object} docRef Document reference.
 * @returns {Object} List of objects.
 * @private
 */
const getCollection = function (docRef) {
  return new Promise((resolve, reject) => {
    docRef.get().then((snapshot) => {
      var docList = []
      snapshot.forEach((doc) => {
        docList.push(doc.data())
      })
      resolve(docList)
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then((results) => {
    return results
  }).catch((err) => {
    throw err
  })
}

/**
 * Delete a collection.
 *
 * @param {Object} colRef Collection reference.
 * @param {number} batchSize Delete batch size.
 * @private
 */
const deleteCollection = function (colRef, batchSize = 100) {
  const query = colRef.limit(batchSize)
  return new Promise((resolve, reject) => {
    deleteBatch(firebase.firestore(), query, batchSize, resolve, reject)
  })
}

/**
 * Delete a document.
 *
 * @param {Object} docRef Document reference.
 * @private
 */
const deleteDocument = function (docRef) {
  return new Promise((resolve, reject) => {
    docRef.delete().then(() => {
      resolve()
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then(() => {

  }).catch((err) => {
    throw err
  })
}

/**
 * Get a list of objects from a query document reference.
 *
 * @param {Object} docRef Document reference.
 * @returns {Object} List of found objects.
 * @private
 */
const getDocumentByQuery = function (docRef) {
  return new Promise((resolve, reject) => {
    docRef.get().then((snapshot) => {
      if (snapshot.size <= 0) {
        reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 'Path: {0}'.format(docRef.path)))
      } else {
        var docList = []
        snapshot.forEach((doc) => {
          docList.push(doc.data())
        })
        resolve(docList)
      }
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then((results) => {
    return results
  }).catch((err) => {
    throw err
  })
}

/**
 * Save a document.
 *
 * @param {Object} docRef Document reference.
 * @param {Object} obj Object to save.
 * @returns {Object} Saved object.
 * @private
 */
const saveDocument = function (docRef, obj) {
  const docData = JSON.stringify(obj)
  return new Promise((resolve, reject) => {
    docRef.set(JSON.parse(docData), { merge: true }).then(() => {
      resolve(obj)
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then((results) => {
    return results
  }).catch((err) => {
    throw err
  })
}

/**
 * Get collection count.
 *
 * @param {Object} docRef Document reference.
 * @returns {Object} Count of documents.
 * @private
 */
const getCollectionCount = function (docRef) {
  return new Promise((resolve, reject) => {
    docRef.get().then((snapshot) => {
      resolve(snapshot.size)
    }).catch((err) => {
      reject(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err))
    })
  }).then((results) => {
    return results
  }).catch((err) => {
    throw err
  })
}

module.exports = {
  getDoc: getDoc,
  getCol: getCol,
  saveDoc: saveDoc,
  deleteDoc: deleteDoc,
  getDocByQuery: getDocByQuery,
  getColCount: getColCount,

  // Promise based functions
  getDocument: getDocument,
  getCollection: getCollection,
  deleteCollection: deleteCollection,
  deleteDocument: deleteDocument,
  getDocumentByQuery: getDocumentByQuery,
  saveDocument: saveDocument,
  getCollectionCount: getCollectionCount
}
