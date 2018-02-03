'use strict'

const config = require('./env.json')[process.env.NODE_ENV || 'development']
const functions = require('firebase-functions')
const admin = require('firebase-admin')
const express = require('express')
const engines = require('consolidate')
const path = require('path')
const fs = require('fs')
const cookieParser = require('cookie-parser')
const bodyParser = require('body-parser')
const HttpStatus = require('http-status-codes')
const constants = require('./shared/constants')

const index = require('./routes/index')
const queue = require('./routes/queue')
const entity = require('./routes/entity')
const lookup = require('./routes/lookup')
const history = require('./routes/history')
const archive = require('./routes/archive')

const app = express()

// Call ONCE only during start-up
var serviceAccount = JSON.parse(fs.readFileSync(path.join(__dirname, config.service_account), { encoding: 'utf8' }))
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: config.database_url,
  storageBucket: config.storage_bucket
})
// admin.initializeApp(functions.config().firebase)

// Express middleware that validates Firebase ID Tokens passed in the Authorization HTTP header.
// The Firebase ID token needs to be passed as a Bearer token in the Authorization HTTP header like this:
// 'Authorization: Bearer <Firebase ID Token>'.
// when decoded successfully, the ID Token content will be added as `req.user`.
const authenticate = (req, res, next) => {
  if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer ')) {
    res.status(HttpStatus.FORBIDDEN).send(constants.Unauthorized)
    return
  }
  const idToken = req.headers.authorization.split('Bearer ')[1]
  admin.auth().verifyIdToken(idToken).then(decodedIdToken => {
    req.user = decodedIdToken
    next()
  }).catch(error => {
    console.error(error)
    res.status(HttpStatus.FORBIDDEN).send(constants.Unauthorized)
  })
}

// Skipping authentication for now - UNCOMMENT later for security authentication
// app.use(authenticate)

// view engine setup
app.engine('hbs', engines.handlebars)
app.set('views', path.join(__dirname, 'views'))
app.set('view engine', 'hbs')

app.use(bodyParser.json())
app.use(bodyParser.urlencoded({ extended: false }))
app.use(cookieParser())

app.use('/', index)
app.use('/entity', entity)
app.use('/queue', queue)
app.use('/lookup', lookup)
app.use('/history', history)
app.use('/archive', archive)

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  var err = new Error('Not Found')
  err.status = 404
  next(err)
})

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message
  res.locals.error = req.app.get('env') === 'development' ? err : {}

  // render the error page
  // res.status(err.status || 500)
  // res.render('error', { partials: Object.assign({}, constants.Partials) })
  if (req.app.get('env') === 'development') {
    res.status(err.status || 500).json(err)
  } else {
    res.status(err.status || 500).json(err.message)
  }
})

exports.app = functions.https.onRequest(app)
