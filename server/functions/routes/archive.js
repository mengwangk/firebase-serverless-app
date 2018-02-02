'use strict'

const express = require('express')
const HttpStatus = require('http-status-codes')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 * Get list of archived bookings for a particular entity.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId   // entity id

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getArchives(callback, entityId)
})

/**
 * Delete archived bookings for an entity.
 * @public
 */
router.delete('/:entityId', function (req, res, next) {

})

/**
 * Delete a particular archive for an entity.
 * @public
 */
router.delete('/:entityId/:archiveId', function (req, res, next) {

})

module.exports = router
