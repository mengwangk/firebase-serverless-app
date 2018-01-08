'use strict';

const functions = require('firebase-functions');
const express = require('express');
const engines = require('consolidate');
const path = require('path');
const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');
const constants = require('./shared/constants');

const index = require('./routes/index');
const queue = require('./routes/queue');
const entity = require('./routes/entity');

const app = express();

// view engine setup
app.engine('hbs', engines.handlebars);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'hbs');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());

app.use('/', index);
app.use('/entity', entity);
app.use('/queue', queue);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handler
app.use(function(err, req, res, next) {
    // set locals, only providing error in development
    res.locals.message = err.message;
    res.locals.error = req.app.get('env') === 'development' ? err : {};

    // render the error page
    res.status(err.status || 500);
    res.render('error', { partials: Object.assign({}, constants.Partials)})
});

exports.app = functions.https.onRequest(app);
