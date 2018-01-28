'use strict'

const express = require('express')
const constants = require('../shared/constants')
const utils = require('../shared/utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Queue = require('../models/queue')
const Entity = require('../models/entity')
const FirebaseUtils = require('../shared/firebase-utils')
const router = express.Router()

/**
 * Get a list of entities. Filtered by email if it is passed in.
 * @public
 */
router.get('/', function (req, res, next) {
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  let email = req.query.email
  if (email) {
    // Retrieve entity using email
    FirebaseUtils.fireStore.getEntitiesByEmail(callback, email)
  } else {
    // Retrieve all entities
    FirebaseUtils.fireStore.getEntities(callback)
  }
})

/**
 * Get a particular entity.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId   // entity id
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getEntities(callback, entityId)
})

/**
  * Create an entity.
  * @public
  */
router.post('/', function (req, res, next) {
  if (!req.body.entity) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Get the entity
  const data = req.body.entity
  const entity = new Entity(data.name, data.email)

  // Validate the entity
  if (!entity.name || !entity.email) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.entity))
    return
  }

  // Map the remaining entity values from the request
  utils.Mapper.assign(entity, data)

  // Save the entity
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.CREATED).json(results)
    }
  }
  FirebaseUtils.fireStore.saveEntity(callback, entity)
})

/**
  * Update an entity.
  * @public
  */
router.put('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId

  if (!req.body.entity) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  // Get the entity
  const data = req.body.entity
  const entity = new Entity(data.name, data.email, entityId)

  // Validate the entity
  if (!entity.id || !entity.name || !entity.email) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.entity))
    return
  }

  // Map the remaining entity values from the request
  utils.Mapper.assign(entity, data)

  // Save the entity
  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.saveEntity(callback, entity)
})

/**
 * Get a list of queues belonged to this entity.
 * @public
 */
router.get('/:entityId/queue', function (req, res, next) {
  const entityId = req.params.entityId

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getQueues(callback, entityId)
})

/**
 * Get a particular queue.
 * @public
 */
router.get('/:entityId/queue/:queueId', function (req, res, next) {
  const entityId = req.params.entityId
  const queueId = req.params.queueId

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.getQueues(callback, entityId, queueId)
})

/**
 * Create a queue for this entity.
 * @public
 */
router.post('/:entityId/queue', function (req, res, next) {
  const entityId = req.params.entityId

  if (!req.body.queue) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  const data = req.body.queue
  const queue = new Queue(data.name, data.minCapacity, data.maxCapacity, data.prefix)

  if (!queue.name) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.queue))
    return
  }
  // Map the remaining queue values from the request
  utils.Mapper.assign(queue, data)

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.CREATED).json(results)
    }
  }
  FirebaseUtils.fireStore.saveQueue(callback, entityId, queue)  // Save the queue
})

/**
 * Update a queue.
 * @public
 */
router.put('/:entityId/queue/:queueId', function (req, res, next) {
  const entityId = req.params.entityId
  const queueId = req.params.queueId

  if (!req.body.queue || !req.body.queue.id) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData))
    return
  }

  const data = req.body.queue
  const queue = new Queue(data.name, data.minCapacity, data.maxCapacity, data.prefix, queueId)

  if (!queue.id || !queue.name) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, req.body.queue))
    return
  }

  // Map the remaining entity values from the request
  utils.Mapper.assign(queue, data)

  let callback = (results, err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.OK).json(results)
    }
  }
  FirebaseUtils.fireStore.saveQueue(callback, entityId, queue)  // Save the queue
})

/**
 * Delete a queue.
 * @public
 */
router.delete('/:entityId/queue/:queueId', function (req, res, next) {
  const entityId = req.params.entityId
  const queueId = req.params.queueId

  let callback = (results = '', err = null) => {
    if (err != null) {
      res.status(err.statusCode).json(err)
    } else {
      res.status(HttpStatus.ACCEPTED).json(constants.QueueDeleted)
    }
  }

  FirebaseUtils.fireStore.deleteQueue(callback, entityId, queueId)
})

module.exports = router
