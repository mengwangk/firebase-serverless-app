const firebase = require('firebase-admin');
const functions = require('firebase-functions');

const FireStore = (function() {
    var self = {};

    const firebaseApp = firebase.initializeApp(
        functions.config().firebase
    );
    
    self.saveOrUpdate = function(docPath, obj){
        if (!obj.id) {
            // Insert
        } else {
            // Update
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