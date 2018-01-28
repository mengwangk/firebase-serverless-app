'use strict'

const express = require('express')
const constants = require('../shared/constants')

const router = express.Router()

/**
 * Default home route.
 * @public
 */
router.get('/', function (req, res, next) {
  res.render('index', { appName: constants.AppName, partials: Object.assign({}, constants.Partials) })
})

module.exports = router
