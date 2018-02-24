'use strict'

const config = require('../env.json')[process.env.NODE_ENV || 'development']
const express = require('express')
const HttpStatus = require('http-status-codes')
const constants = require('../shared/constants')
const ApplicationError = require('../models/application-error')
const nodemailer = require('nodemailer')

const router = express.Router()

// Configure the email transport using the default SMTP transport and a Gmail account.
// For Gmail, enable these:
// 1. https://www.google.com/settings/security/lesssecureapps
// 2. https://accounts.google.com/DisplayUnlockCaptcha
// For other types of transports such as Sendgrid see https://nodemailer.com/transports/
// TODO: Configure the `gmail.email` and `gmail.password` Google Cloud environment variables.
const gmailEmail = config.email
const gmailPassword = config.emailPassword
const mailTransport = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword
  }
})

/**
 * Send a support email.
 *
 * @public
 */
router.post('/', function (req, res, next) {
  const subject = req.body.subject
  const message = req.body.message
  
  if (!subject || !message) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  const mailOptions = {
    from: `${config.app_name} <noreply@${config.app_name}.com>`,
    to: gmailEmail
  }

  mailOptions.subject = subject
  mailOptions.text = message
  return mailTransport.sendMail(mailOptions).then(() => {
    res.status(HttpStatus.NO_CONTENT).json()
  }).catch((err) => {
    res.status(HttpStatus.BAD_REQUEST).json(err)
  })
})

module.exports = router
