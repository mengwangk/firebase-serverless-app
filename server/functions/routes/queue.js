const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const firebase = require('firebase-admin');
const functions = require('firebase-functions');
const Customer = require('../models/customer');

const firebaseApp = firebase.initializeApp(
    functions.config().firebase
);

const router = express.Router();

router.get('/', function (req, res, next) {
    const database = firebase.firestore();
    var data = database.collection('restaurant1').doc("queue").collection("queue1").doc("1485fabc-ee4b-47a8-88bb-5971acf8a71d").get()
            .then((results) => {
                console.log("data => " + JSON.stringify(results.data()));
                /* 
                for (var doc in snapshot) {
                    snapshot.forEach(doc => {
                        console.log(doc.id, '=>', doc.data());
                    });
                }
                */
                res.json("ok");
            })
            .catch((err) => {
                console.log('Error getting documents', err);
                res.json("error");
            });
});


router.post('/:name/:queue', function (req, res, next) {
    // Get the shop name

    // Get the target queue name

    // Get the customer info


    // Get the customer info from request body
    var customer = new Customer("customer 3", "1234567890");
    var data = JSON.stringify(customer);

    const database = firebase.firestore();
    var queue = database.collection("restaurant1").doc("queue").collection("queue1").doc(customer.id);
    var result = queue.set(JSON.parse(data));
    res.json(data);
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
