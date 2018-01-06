'use strict';

const functions = require('firebase-functions');
const firebase = require('firebase-admin');
const express = require('express');
const engines = require('consolidate');
const path = require('path');
const favicon = require('serve-favicon');
const logger = require('morgan');
const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');
const cors = require('cors');

const firebaseApp = firebase.initializeApp(
    functions.config().firebase
);

function getFacts(){
    const ref = firebaseApp.database().ref('facts');
    return ref.once('value').then(snap => snap.val());
}

const app = express();

// view engine setup
app.engine('mustache', engines.mustache);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'mustache');

app.get('/', (request, response) => {
    response.set('Cache-Control', 'public, max-age=300, s-maxage=600');
    getFacts().then(facts => {
        response.render('index', {'name': 'hello world'});
    });
});

exports.app = functions.https.onRequest(app);
