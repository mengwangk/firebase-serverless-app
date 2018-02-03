'use strict'

const express = require('express')
const constants = require('../shared/constants')
const utils = require('../shared/utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Queue = require('../models/queue')
const Entity = require('../models/entity')
const FirebaseUtils = require('../shared/firebase-utils')
const formidable = require('formidable')
const router = express.Router()

/**
 * Get a list of entities. Filtered by email if it is passed in.
 * @public
 */
router.get('/', function (req, res, next) {
  let email = req.query.email
  if (email) {
    // Retrieve entity using email
    FirebaseUtils.fireStore.getEntitiesByEmail(email).then((results) => {
      res.status(HttpStatus.OK).json(results)
    }).catch((err) => {
      res.status(err.statusCode).json(err)
    })
  } else {
    // Retrieve all entities
    FirebaseUtils.fireStore.getEntities().then((results) => {
      res.status(HttpStatus.OK).json(results)
    }).catch((err) => {
      res.status(err.statusCode).json(err)
    })
  }
})

/**
 * Get a particular entity.
 * @public
 */
router.get('/:entityId', function (req, res, next) {
  const entityId = req.params.entityId   // entity id
  FirebaseUtils.fireStore.getEntities(entityId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Create the entity only. Refer to /user if you want to create the firebase user also.
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
  FirebaseUtils.fireStore.saveEntity(entity).then((results) => {
    res.status(HttpStatus.CREATED).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
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
  FirebaseUtils.fireStore.saveEntity(entity).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Create the firebase login user and entity with avatar. Using multipart/form-data.
 * @public
 */
router.post('/user', function (req, res, next) {
  // parse a file upload
  const form = new formidable.IncomingForm()
  form.parse(req, function (err, fields, files) {
    console.log('error => ' + JSON.stringify(err))
    console.log('fields => ' + JSON.stringify(fields))
    console.log('files => ' + JSON.stringify(files))
    
    const entityRequest = JSON.parse(fields.entityRequest)
    const data = entityRequest.entity
    const password = entityRequest.password

    // Validate the entity
    if (!data.name || !data.email || !password) {
      res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, fields.entityRequest))
      return
    }

    // Create the entity
    const entity = new Entity(data.name, data.email)

    // Validate the uploaded file
    const avatarFile = files.avatar
    var storagePath = null
    if (avatarFile) {
        // Set avatar path
      storagePath = utils.Upload.createStoragePath(entity, avatarFile)
      entity.avatar = storagePath
    }
      // Map the remaining entity values from the request
    utils.Mapper.assign(entity, data)

    if (avatarFile) {
        // Check file size
      if (utils.Upload.hasExceedMaxAllowedSize(avatarFile.size)) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.FileExceededLimit))
        return
      }
        // Check file type
      if (!utils.Upload.isFileTypeAllowed(avatarFile.name)) {
        res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.FileTypeNotAllowed))
        return
      }
    }

      // Create the firebase user
    FirebaseUtils.auth.createUser(data.email, password).then(
        function (userRecord) {
          let callback = (results, err = null) => {
            if (err != null) {
              res.status(err.statusCode).json(err)
            } else {
              if (avatarFile) {
                FirebaseUtils.storage.upload(entity, avatarFile).then(function (data) {
                  // var file = data[0];
                  // console.log('file --> ' + JSON.stringify(entity.avatar))
                  res.status(HttpStatus.CREATED).json(results)
                }).catch(function (error) {
                  // Since user is already created, the upload error is ignored
                  console.error(error)
                  res.status(HttpStatus.CREATED).json(results)
                })
              } else {
                res.status(HttpStatus.CREATED).json(results)
              }
            }
          }
          // Create the entity
          FirebaseUtils.fireStore.saveEntity(callback, entity)
        }
      ).catch(function (error) {
        console.error(error)
        res.status(HttpStatus.SERVICE_UNAVAILABLE).json(new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.UserCreationError))
      })
  })
})

/**
 * Get a list of queues belonged to this entity.
 * @public
 */
router.get('/:entityId/queue', function (req, res, next) {
  const entityId = req.params.entityId
  FirebaseUtils.fireStore.getQueues(entityId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Get a particular queue.
 * @public
 */
router.get('/:entityId/queue/:queueId', function (req, res, next) {
  const entityId = req.params.entityId
  const queueId = req.params.queueId
  FirebaseUtils.fireStore.getQueues(entityId, queueId).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
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

  // Save the queue
  FirebaseUtils.fireStore.saveQueue(entityId, queue).then((results) => {
    res.status(HttpStatus.CREATED).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
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

  // Update the queue
  FirebaseUtils.fireStore.saveQueue(entityId, queue).then((results) => {
    res.status(HttpStatus.OK).json(results)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

/**
 * Delete a queue.
 * @public
 */
router.delete('/:entityId/queue/:queueId', function (req, res, next) {
  const entityId = req.params.entityId
  const queueId = req.params.queueId
  FirebaseUtils.fireStore.deleteQueue(entityId, queueId).then((results) => {
    res.status(HttpStatus.ACCEPTED).json(constants.QueueDeleted)
  }).catch((err) => {
    res.status(err.statusCode).json(err)
  })
})

module.exports = router
