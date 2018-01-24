'use strict'

const express = require('express')
const HttpStatus = require('http-status-codes')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 * Return all lookup data.
 *
 * @public
 */
router.get('/', function (req, res, next) {
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getLookup(callback, null)
})

/**
 * Get all the industries.
 * @public
 */
router.get('/:lookupType', function (req, res, next) {
  const lookupType = req.params.lookupType
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getLookup(callback, lookupType)
})

module.exports = router
