const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const HttpStatus = require('http-status-codes');
const ApplicationError = require('../models/application-error');
const Queue = require('../models/queue');
const Booking = require('../models/booking');
const FirebaseUtils = require('../shared/firebase-utils.js');
const router = express.Router();

// Get bookings under a queue
router.get('/:entityId/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    const queueId = req.params.queueId;     // queue id

    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getBookings(callback, entityId, queueId);
});

// Save booking to a queue
router.post('/:entityId/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    const queueId = req.params.queueId;     // queue id

    if (!req.body.booking) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, "Data: {0}".format(req.body.booking)));
        return;
    }

    // Get the booking and queue info from request body
    const data = req.body.booking;
    const booking = new Booking(data.name, data.contactNo, data.noOfCustomers);
   
    // TODO - validate the booking data

    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.status(HttpStatus.CREATED).json(results);
        }
    };
    FirebaseUtils.fireStore.saveBooking(callback, entityId, queueId, booking);  // Save the booking info
});

// Clear a queue
router.delete('/:entityId/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    const queueId = req.params.queueId;     // queue id

    // Delete all bookings
    FirebaseUtils.fireStore.clearQueue(entityId, queueId);
    res.status(HttpStatus.ACCEPTED).json(constants.BatchQueueDelete);
});

// Delete a booking
router.delete('/:entityId/:queueId/:bookingId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    const queueId = req.params.queueId;     // queue id
    const bookingId = req.params.bookingId;     // queue id

    let callback = (results = "", err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.status(HttpStatus.NO_CONTENT).json(constants.BookingDeleted);
        }
    };

    // Delete the booking info
    FirebaseUtils.fireStore.deleteBooking(callback, entityId, queueId, bookingId);
});

module.exports = router;