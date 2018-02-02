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
  FirebaseUtils.fireStore.getLookup().then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Get all the industries.
 * @public
 */
router.get('/:lookupType', function (req, res, next) {
  const lookupType = req.params.lookupType
  FirebaseUtils.fireStore.getLookup(lookupType).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

module.exports = router
