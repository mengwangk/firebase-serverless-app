'use strict'

const express = require('express')
const constants = require('../shared/constants')
const config = require('../env.json')[process.env.NODE_ENV || 'development']
const router = express.Router()

/**
 * Default home route.
 * @public
 */
router.get('/', function (req, res, next) {
  res.render('index', { appName: config.app_name, partials: Object.assign({}, constants.Partials) })
})

module.exports = router
