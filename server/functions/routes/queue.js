const express = require('express');
const path = require('path');
const constants = require('./constants');
const firebase = require('firebase-admin');

const firebaseApp = firebase.initializeApp(
    functions.config().firebase
);

const router = express.Router();

router.post('/', function (req, res, next) {
    res.set('Cache-Control', 'public, max-age=300, s-maxage=600');
});

module.exports = router;

/* 
function getFacts(){
    const ref = firebaseApp.database().ref('facts');
    return ref.once('value').then(snap => snap.val());
}
*/

/*
app.get('/', (request, response) => {
    response.set('Cache-Control', 'public, max-age=300, s-maxage=600');
    getFacts().then(facts => {
        response.render('index', {'name': 'hello world'});
    });
});
*/
