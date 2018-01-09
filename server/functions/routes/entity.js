
const express = require('express');
const path = require('path');
const constants = require('../shared/constants');
const HttpStatus = require('http-status-codes');
const ApplicationError = require('../models/application-error');
const Queue = require('../models/queue');
const Entity = require('../models/entity');
const FirebaseUtils = require('../shared/firebase-utils.js');
const router = express.Router();


 // Create an entity
router.post('/', function (req, res, next) {
    if (!req.body.entity) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, "entity: {0}".format(req.body.entityt)));
        return;
    }

    // Get the customer and queue info from request body
    const data = JSON.parse(req.body.entity); // customer info
    const entity = new Entity(data.name);
  
    // TODO - validate the entity 


    // Save the customer info
    const result = FirebaseUtils.fireStore.saveEntity(entity);
    res.json(result);
});

// Get all entities
router.get('/', function (req, res, next) {
    let callback = (results, err = null) => {
        if (err != null) {
            res.status(500).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getEntities(callback);
});

// Get all entities
router.get('/:entityId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    let callback = (results, err = null) => {
        if (err != null) {
            res.status(400).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getEntities(callback, entityId);
});


// Get all the 
router.get('/:entityId/queue', function (req, res, next) {

});


// Create a queue for an entity
router.post('/:entity/queue', function (req, res, next) {

});

module.exports = router;
