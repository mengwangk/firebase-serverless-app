'use strict'

const express = require('express')
const constants = require('../shared/constants')
const HttpStatus = require('http-status-codes')
const FirebaseUtils = require('../shared/firebase-utils')
const ApplicationError = require('../models/application-error')
const router = express.Router()

/**
 * Get all historical bookings under this entity id.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId
  FirebaseUtils.fireStore.getHistories(entityId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Return the booking to the respective queue.
 * @public
 */
router.delete('/:entityId/:queueId/:bookingId/:action', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Get the queue id
  const queueId = req.params.queueId

  // Get the booking id
  const bookingId = req.params.bookingId

  // Action - to return the booking
  const action = req.params.action.toLowerCase()

  // Validate the action
  if (action !== constants.HistoryAction.return) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Return the historical booking
  FirebaseUtils.fireStore.returnHistory(action, entityId, queueId, bookingId).then((results) => {
    res.status(HttpStatus.OK).json(constants.HistoryUpdated)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Archive all historical bookings under this entity.
 * @public
 */
router.delete('/:entityId/:action', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Action - to archive all historical bookings
  const action = req.params.action.toLowerCase()

  // Validate the action - archive only
  if (action !== constants.HistoryAction.archive) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Archive the historical booking
  FirebaseUtils.fireStore.archiveHistory(entityId).then((results) => {
    res.status(HttpStatus.ACCEPTED).json(constants.BatchArchive)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

module.exports = router
