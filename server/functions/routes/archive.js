'use strict'

const express = require('express')
const HttpStatus = require('http-status-codes')
const FirebaseUtils = require('../shared/firebase-utils')
const constants = require('../shared/constants')
const router = express.Router()

/**
 * Get list of archived bookings for a particular entity.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId
  FirebaseUtils.fireStore.getArchives(entityId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Get list of archived bookings for a particular archive id.
 * @public
 */
router.get('/:entityId/:archiveId', function (req, res, next) {
  const entityId = req.params.entityId
  const archiveId = req.params.archiveId
  FirebaseUtils.fireStore.getArchive(entityId, archiveId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Delete archived bookings for an entity.
 *
 * @public
 */
router.delete('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId

  // Proceed to delete the archives - NOT IMPLEMENTED. Use another delete method below.
  FirebaseUtils.fireStore.deleteAllArchives(entityId)

  // Do not wait for the deletion to complete
  res.status(HttpStatus.ACCEPTED).json(constants.BatchClearArchive)
})

/**
 * Delete a list of archives for an entity.
 * @public
 */
router.delete('/:entityId/:archiveIds', function (req, res, next) {
  const entityId = req.params.entityId
  const archiveIds = req.params.archiveIds  // List of comma separated archive ids

  // Proceed to delete the archives
  FirebaseUtils.fireStore.deleteArchives(entityId, archiveIds)

  // Do not wait for the deletion to complete
  res.status(HttpStatus.ACCEPTED).json(constants.BatchClearArchive)
})

module.exports = router
