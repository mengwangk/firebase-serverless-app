'use strict'

const express = require('express')
const constants = require('../shared/constants')
const HttpStatus = require('http-status-codes')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 * Get all historical bookings under this entity id.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getHistories(callback, entityId)
})

/**
 * Return or archive the booking to the respective queue.
 * @public
 */
router.delete('/:entityId/:queueId/:bookingId/:action', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Get the queue id
  const queueId = req.params.queueId

  // Get the booking id
  const bookingId = req.params.bookingId

  // Action - to "return" or "archive" the booking
  const action = req.params.action.toLowerCase()

  // Validate the action - either to return or archive
  if (action !== constants.HistoryAction.return && action !== constants.HistoryAction.archive) {
    res.status(HttpStatus.BAD_REQUEST).json(constants.InvalidData)
    return
  }

  let callback = (results = '', err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(constants.HistoryUpdated)
    }
  }

  // Return or archive the historical booking
  FirebaseUtils.fireStore.deleteHistory(callback, action, entityId, queueId, bookingId)
})

/**
 * Archive all historical bookings under this entity.
 * @public
 */
router.delete('/:entityId/:action', function (req, res, next) {
  // Get the entity id
  const entityId = req.params.entityId

  // Action - to "return" or "archive" all historical bookings
  const action = req.params.action.toLowerCase()

  // Validate the action - archive only
  if (action !== constants.HistoryAction.archive) {
    res.status(HttpStatus.BAD_REQUEST).json(constants.InvalidData)
    return
  }

  let callback = (results = '', err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.ACCEPTED).json(constants.BatchArchive)
    }
  }

  // Return or archive the historical booking
  FirebaseUtils.fireStore.archiveHistory(callback, action, entityId)
})

module.exports = router
