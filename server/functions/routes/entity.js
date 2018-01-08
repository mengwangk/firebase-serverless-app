
const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const ApplicationError = require('../models/application-error');
const Queue = require('../models/queue');
const Entity = require('../models/entity');
const FirebaseUtils = require('../shared/firebase-utils.js');
const router = express.Router();


 // Create an entity
router.post('/', function (req, res, next) {
    if (!req.body.entity) {
        res.status(400).json(new ApplicationError(constants.InvalidData, "entity: {0}".format(req.body.entityt)));
        return;
    }

    // TODO - check if entity and queue exist


    // Get the customer and queue info from request body
    const data = JSON.parse(req.body.customer); // customer info
    const customer = new Customer(data.name, data.contactNo);
  
    // TODO - validate the customer data


    // Save the customer info
    const result = FirebaseUtils.fireStore.queue(entityId, queueId, customer);
    res.json(result);
});

// Create a queue for an entity
router.post('/queue', function (req, res, next) {

});

module.exports = router;
