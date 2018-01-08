
const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const ApplicationError = require('../models/application-error');
const Queue = require('../models/queue');
const Customer = require('../models/customer');
const FirebaseUtils = require('../shared/firebase-utils.js');
const router = express.Router();

// Get all queues under an entity
/*
router.get('/:entityId', function (req, res, next) {
    const name = req.params.name;   // shop name
    const database = firebase.firestore();
    database.collection(name).get()    
            .then((snapshot) => {
                snapshot.forEach((doc) => {
                    // console.log(doc.id, '=>', doc.get("name"));
                });
                res.json(customers);
            })
            .catch((err) => {
                res.status(500).json(new ApplicationError(constants.ServerError, err));
            });
});

// for testing only -- add the queue
const queue = new Queue(queueId, "queue description...");
var docRef = database.collection(entityId).doc(queueId);
var doc = docRef.set(JSON.parse(JSON.stringify(queue)));


*/

router.post('/:entityId/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    const queueId = req.params.queueId;     // queue id

    if (!req.body.customer) {
        res.status(400).json(new ApplicationError(constants.InvalidData, "name: {0}, queue: {1}, customer: {2}".format(entityId, queueId, req.body.customer)));
        return;
    }

    // TODO - check if entity and queue exist


    // Get the customer and queue info from request body
    const data = JSON.parse(req.body.customer); // customer info
    const customer = new Customer(data.name, data.contactNo);
  
    // TODO - validate the customer data


    // Save the customer info
    const result = FirebaseUtils.fireStore.queue(entityId, queueId, customer);
    res.json(result);
});

module.exports = router;

/* 
function getFacts(){
    const ref = firebaseApp.database().ref('facts');
    return ref.once('value').then(snap => snap.val());
}

app.get('/', (request, response) => {
    response.set('Cache-Control', 'public, max-age=300, s-maxage=600');
    getFacts().then(facts => {
        response.render('index', {'name': 'hello world'});
    });
});
*/
