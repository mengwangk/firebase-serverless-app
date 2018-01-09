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
    
    self.saveEntity = function(entity){
        const docData = JSON.stringify(entity);
        const database = firebase.firestore();
        docRef = database.collection(constants.EntityCollection).doc(entity.id);
        doc = docRef.set(JSON.parse(docData), { merge: true });
        return docData;
    }

    self.getEntities = function(callback, entityId = null){
        const database = firebase.firestore();
        if (entityId != null) {
            database.collection(constants.EntityCollection).doc(entityId).get()    
            .then((doc) => {
               if (!doc.exists) {
                    callback(null, new ApplicationError(HttpStatus.NOT_FOUND, constants.NoRecordFound, "entityId: {0}".format(entityId)));
               } else {
                    callback(doc.data());
               }
            })
            .catch((err) => {
                callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
            });
        } else {
            // Retrieve all entities
            database.collection(constants.EntityCollection).get()    
                .then((snapshot) => {
                    var entities = [];
                    snapshot.forEach((doc) => {
                        entities.push(doc.data());
                    });
                    callback(entities);
                })
                .catch((err) => {
                    callback(null, new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
                });
        }
    }




    
    self.queue = function(entityId, queueId, customer) {
       const docData = JSON.stringify(customer);
       const database = firebase.firestore();
       docRef = database.collection(entityId).doc(queueId).collection(queueId).doc(customer.id);
       doc = docRef.set(JSON.parse(docData));
       return docData;
    }
    return self;

  })();
  
  
  module.exports = {
      fireStore: FireStore
  }