'use strict'

const express = require('express')
const constants = require('../shared/constants')
const utils = require('../shared/utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 *
 * @public
 */
router.get('/', function (req, res, next) {

})

module.exports = router
