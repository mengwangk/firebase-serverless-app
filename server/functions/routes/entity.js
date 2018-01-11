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
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, "Data: {0}".format(req.body.entity)));
        return;
    }

    // Get the entity
    const data = req.body.entity; 
    const entity = new Entity(data.name, data.email);
  
    // TODO - validate the entity 


    // Save the entity
    const result = FirebaseUtils.fireStore.saveEntity(entity);
    res.status(HttpStatus.CREATED).json(result);
});

// Get all entities
router.get('/', function (req, res, next) {
    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getEntities(callback);
});

// Get a particular entity
router.get('/:entityId', function (req, res, next) {
    const entityId = req.params.entityId;   // entity id
    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getEntities(callback, entityId);
});


// Get all the queues belonged to this entity
router.get('/:entityId/queue', function (req, res, next) {
    const entityId = req.params.entityId;

    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getQueues(callback, entityId);
});

// Get a particular queue
router.get('/:entityId/queue/:queueId', function (req, res, next) {
    const entityId = req.params.entityId;
    const queueId = req.params.queueId;

    let callback = (results, err = null) => {
        if (err != null) {
            res.status(err.statusCode).json(err);
        } else {
            res.json(results);
        }
    };
    FirebaseUtils.fireStore.getQueues(callback, entityId, queueId);
});

// Create a queue for an entity
router.post('/:entityId/queue', function (req, res, next) {
    const entityId = req.params.entityId;

    // TODO - check if entity exists before adding queue

    if (!req.body.queue) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, "Data: {0}".format(req.body.queue)));
        return;
    }

    const data = req.body.queue; 
    const queue = new Queue(data.name, data.capacity, data.prefix);
  
    // TODO - validate the queue

    
    const result = FirebaseUtils.fireStore.saveQueue(entityId, queue);  // Save the queue
    res.status(HttpStatus.CREATED).json(result);
});

module.exports = router;
