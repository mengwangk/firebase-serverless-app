'use strict'

const express = require('express')
const constants = require('../shared/constants')
const utils = require('../shared/utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Booking = require('../models/booking')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 * Get bookings under a queue.
 * @public
 */
router.get('/:entityId/:queueId', function (req, res, next) {
  const entityId = req.params.entityId   // entity id
  const queueId = req.params.queueId     // queue id

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getBookings(callback, entityId, queueId)
})

/**
 * Save booking under a queue.
 * @public
 */
router.post('/:entityId/:queueId', function (req, res, next) {
  const entityId = req.params.entityId   // entity id
  const queueId = req.params.queueId     // queue id

  if (!req.body.booking) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Get the booking info from request body
  const data = req.body.booking
  const booking = new Booking(data.name, data.contactNo, data.noOfSeats)

  // Validate the booking
  if (!booking.name || !booking.contactNo) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.booking))
    return
  }

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.CREATED).json(results)
    }
  }
  FirebaseUtils.fireStore.saveBooking(callback, entityId, queueId, booking)  // Save the booking info
})

/**
 * Update booking under a queue.
 * @public
 */
router.put('/:entityId/:queueId/:bookingId', function (req, res, next) {
  const entityId = req.params.entityId      // entity id
  const queueId = req.params.queueId        // queue id
  const bookingId = req.params.bookingId    // booking id

  if (!req.body.booking) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Get the booking info from request body
  const data = req.body.booking
  const booking = new Booking(data.name, data.contactNo, data.noOfSeats, bookingId)

  // Validate the booking
  if (!booking.id || !booking.name || !booking.contactNo) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.booking))
    return
  }
  // Map the remaining booking values from the request
  utils.Mapper.assign(booking, data)

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.saveBooking(callback, entityId, queueId, booking)  // Save the booking info
})

/**
 * Clear a queue (remove all bookings) and reset the counter.
 * @public
 */
router.delete('/:entityId/:queueId/', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Get the queue id
  const queueId = req.params.queueId

  // Clear all bookings
  let callback = (results = '', err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.ACCEPTED).json(constants.BatchQueueDelete)
    }
  }
  FirebaseUtils.fireStore.clearQueue(callback, entityId, queueId)
})

/**
 * Delete a booking.
 * @public
 */
router.delete('/:entityId/:queueId/:bookingId/:action', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Get the queue id
  const queueId = req.params.queueId

  // Get the booking id
  const bookingId = req.params.bookingId

  // Action - to "remove" or "done" with the booking
  const action = req.params.action.toLowerCase()

  // Validate the action
  if (action !== constants.BookingAction.remove && action !== constants.BookingAction.done) {
    res.status(HttpStatus.BAD_REQUEST).json(constants.InvalidData)
    return
  }

  let callback = (results = '', err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.NO_CONTENT).json(constants.BookingDeleted)
    }
  }

  // Delete the booking info
  FirebaseUtils.fireStore.deleteBooking(callback, action, entityId, queueId, bookingId)
})

module.exports = router
