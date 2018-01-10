const firebase = require('firebase-admin');
const functions = require('firebase-functions');
const constants = require('../shared/constants');
const HttpStatus = require('http-status-codes');
const ApplicationError = require('../models/application-error');

const FireStore = (function() {
    var self = {};

    const firebaseApp = firebase.initializeApp(
        functions.config().firebase
    );
    
    const saveDoc = function(docRef, obj) {
        const docData = JSON.stringify(obj);
        const doc = docRef.set(JSON.parse(docData), { merge: true });
        return obj;
    }

    const deleteDoc = function(docRef) {
        const doc = docRef.delete().then( () => {
            // Do nothing now
        }).catch((err) => {
            // Do nothing now
        });
    }

    const getCollection = function(docRef, callback) {
        docRef.get()
            .then((snapshot) => {
                var docList = [];
                snapshot.forEach((doc) => {
                    docList.push(doc.data());
                });
                callback(docList);
            })
            .catch((err) => {
                callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
            });
    }

    const getDoc = function(docRef, callback) {
        docRef.get()
            .then((doc) => {
               if (!doc.exists) {
                    callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, "Path: {0}".format(docRef.path)));
               } else {
                    callback(doc.data());
               }
            })
            .catch((err) => {
                callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
            });
    }

    self.saveEntity = function(entity){
         var docRef = firebase.firestore().collection(constants.EntityCollection).doc(entity.id);
         return saveDoc(docRef, entity);
    }

    self.saveQueue = function(entityId, queue){
        var docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queue.id);
        return saveDoc(docRef, queue);
    }

    self.saveBooking = function(callback, entityId, queueId, booking) {
        var queueDocRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId);
        var bookingDocRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(booking.id);
        var transaction = firebase.firestore().runTransaction(t => {
            return t.get(queueDocRef)
                .then(doc => {
                    if (doc.exists) { 
                        var queue = doc.data();
                        var newCounter = queue.counter + 1;
                        booking.bookingNo = queue.prefix + newCounter.pad(3);
                        var bookingData = JSON.stringify(booking);
                        t.update(queueDocRef, { counter: newCounter });
                        t.set(bookingDocRef, JSON.parse(bookingData));
                        return Promise.resolve(booking);
                    } else {
                        return Promise.reject(new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, 
                            "Path: {0}".format(queueDocRef.path)));
                    }
                });
        })
        .then(results => {
            callback(results);
        })
        .catch(err => {
            callback(null, err);
        });
    }

    self.getEntities = function(callback, entityId = null){
        var docRef = null;
        if (entityId != null) {
            docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId); 
            getDoc(docRef, callback);
        } else {
            docRef = firebase.firestore().collection(constants.EntityCollection); 
            getCollection(docRef, callback);
        }  
    }

    self.getQueues = function(callback, entityId, queueId = null){
        var docRef = null;
        if (queueId != null) {
            docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection).doc(queueId); 
            getDoc(docRef, callback);
        } else {
            docRef = firebase.firestore().collection(constants.EntityCollection).doc(entityId).collection(constants.QueueCollection); 
            getCollection(docRef, callback);
        }  
    }

    self.getBookings = function(callback, entityId, queueId){
        var docRef = null;
        docRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId); 
        getCollection(docRef, callback);
    }

    self.deleteBooking = function(callback, entityId, queueId, bookingId) {
        var docRef = firebase.firestore().collection(constants.QueueCollection).doc(entityId).collection(queueId).doc(bookingId);
        docRef.get()
            .then(doc => {
                if (doc.exists) {
                    deleteDoc(docRef);
                    callback();
                } else {
                    callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, "Path: {0}".format(docRef.path)));
                }
            })
            .catch (err=> {
                callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
            });
    }

    return self;
  })();
  
  
  module.exports = {
      fireStore: FireStore
  }