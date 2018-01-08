const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const firebase = require('firebase-admin');
const functions = require('firebase-functions');
const Customer = require('../models/customer');
const Queue = require('../models/queue');
const ApplicationError = require('../models/application-error');

const firebaseApp = firebase.initializeApp(
    functions.config().firebase
);

const router = express.Router();

// Get all queues under an entity
router.get('/:entityId', function (req, res, next) {
    const name = req.params.name;   // shop name
    const database = firebase.firestore();
    database.collection(name).doc(constants.QueueCategory).collection("queue1").get()    
            .then((data) => {
                var customers = [];
                data.forEach((doc) => {
                    // console.log(doc.id, '=>', doc.get("name"));
                });
                res.json(customers);
            })
            .catch((err) => {
                res.status(500).json(new ApplicationError(constants.ServerError, err));
            });
    /*
    var data = database.collection('restaurant1').doc("queue").collection("queue1").doc("1485fabc-ee4b-47a8-88bb-5971acf8a71d").get()
            .then((results) => {
                console.log("data => " + JSON.stringify(results.data()));
                for (var doc in snapshot) {
                    snapshot.forEach(doc => {
                        console.log(doc.id, '=>', doc.data());
                    });
                }
                res.json("ok");
            })
            .catch((err) => {
                console.log('Error getting documents', err);
                res.json("error");
            });
    */
});


router.post('/:entityId/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;   // shop name
    const queueId = req.params.queueId;  // queue name

    if (!req.body.customer) {
        res.status(400).json(new ApplicationError(constants.InvalidData, "name: {0}, queue: {1}, customer: {2}".format(entityId, queueId, req.body.customer)));
        return;
    }

    console.log("queue id ---> " + queueId);
    // Get the customer and queue info from request body
    const data = JSON.parse(req.body.customer); // customer info
    const customer = new Customer(data.name, data.contactNo);
    const queue = new Queue(queueId, "queue description...");

    // Save the customer info
    const customerDoc = JSON.stringify(customer);
    const database = firebase.firestore();


    var docRef = database.collection(entityId).doc(queueId);
    var doc = docRef.set(JSON.parse(JSON.stringify(queue)));

    docRef = database.collection(entityId).doc(constants.QueueCategory).collection(queueId).doc(customer.id);
    doc = docRef.set(JSON.parse(customerDoc));
    res.json(customerDoc);
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
