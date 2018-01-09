
const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const HttpStatus = require('http-status-codes');
const ApplicationError = require('../models/application-error');
const Queue = require('../models/queue');
const Booking = require('../models/booking');
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
                res.json(bookings);
            })
            .catch((err) => {
                res.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.ServerError, err));
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

    if (!req.body.booking) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, "name: {0}, queue: {1}, booking: {2}".format(entityId, queueId, req.body.booking)));
        return;
    }

    // TODO - check if entity and queue exist


    // Get the booking and queue info from request body
    const data = JSON.parse(req.body.booking); // booking info
    const booking = new Booking(data.name, data.contactNo);
  
    // TODO - validate the booking data


    // Save the booking info
    const result = FirebaseUtils.fireStore.queue(entityId, queueId, booking);
    res.json(result);
});

module.exports = router;
