'use strict'

const path = require('path')
const os = require('os')
const fs = require('fs')
const express = require('express')
const constants = require('../shared/constants')
const utils = require('../shared/utils')
const HttpStatus = require('http-status-codes')
const ApplicationError = require('../models/application-error')
const Queue = require('../models/queue')
const Entity = require('../models/entity')
const FirebaseUtils = require('../shared/firebase-utils')
const Busboy = require('busboy')
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
 * https://cloud.google.com/functions/docs/writing/http#handling_multipart_form_uploads
 *
 * @public
 */
router.post('/user', function (req, res, next) {
  const busboy = new Busboy({ headers: req.headers })
  const formData = {}
  const uploads = {}
  const tmpdir = os.tmpdir()

  // Listen for file upload
  busboy.on('file', function (fieldName, file, fileName, encoding, mimeType) {
    // Capture file info
    const filePath = path.join(tmpdir, fileName)
    uploads[fieldName] = {}
    uploads[fieldName].path = filePath
    uploads[fieldName].file = file
    uploads[fieldName].name = fileName
    uploads[fieldName].encoding = encoding
    uploads[fieldName].mimeType = mimeType

    // Write to tmpdir
    file.pipe(fs.createWriteStream(filePath))

    file.on('data', function (data) {
      uploads[fieldName].size = data.length
    })

    file.on('end', function () {
      // File upload completed
    })
  })

  // Listen for form fields
  busboy.on('field', function (fieldName, val, fieldNameTruncated, valTruncated, encoding, mimeType) {
    formData[fieldName] = val
  })

  // When everything is done
  busboy.on('finish', function () {
    createUser(req, res, formData, uploads)
  })

  // The raw bytes of the upload will be in req.rawBody. Send it to
  // busboy, and get a callback when it's finished.
  busboy.end(req.rawBody)
})

/**
 * Creat the user and entity.
 *
 * @param {Object} req Request object.
 * @param {Object} res Response object.
 * @param {Object} formData Form data.
 * @param {Object} uploads Uploaded file.
 */
const createUser = function (req, res, formData, uploads) {
  const entityRequest = JSON.parse(formData.entityRequest)
  const data = entityRequest.entity
  const password = entityRequest.password

    // Validate the entity
  if (!data.name || !data.email || !password) {
    res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.InvalidData, formData.entityRequest))
    removeUploads(uploads)
    return
  }

  // Create the entity
  const entity = new Entity(data.name, data.email)

  // Validate the uploaded file. The uploaded photo field name is called "avatar"
  const avatarFile = uploads.avatar

  if (avatarFile) {
    // Set avatar path
    entity.avatar = utils.Upload.createStoragePath(entity, avatarFile.name)

    // Check file size
    if (utils.Upload.hasExceedMaxAllowedSize(avatarFile.size)) {
      res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.FileExceededLimit))
      removeUploads(uploads)
      return
    }

      // Check file type
    if (!utils.Upload.isFileTypeAllowed(avatarFile.name)) {
      res.status(HttpStatus.BAD_REQUEST).json(new ApplicationError(HttpStatus.BAD_REQUEST, constants.FileTypeNotAllowed))
      removeUploads(uploads)
      return
    }
  }

  // Map the remaining entity values from the request
  utils.Mapper.assign(entity, data)

  // Create the user
  FirebaseUtils.auth.createUser(data.email, password).then((user) => {
    // Create entity
    return FirebaseUtils.fireStore.saveEntity(entity)
  }).catch((err) => {
    // User creation error
    throw new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.UserCreationError, err)
  }).then((results) => {
    // Proceed to upload
    if (avatarFile) {
      return FirebaseUtils.storage.upload(entity, avatarFile.path)
    }
  }).catch((err) => {
    // Entity creation error
    if (err instanceof ApplicationError) {
      throw err
    } else {
      throw new ApplicationError(HttpStatus.SERVICE_UNAVAILABLE, constants.EntityCreationError, err)
    }
  }).then(() => {
    removeUploads(uploads)
    res.status(HttpStatus.CREATED).json(entity)
  }).catch((err) => {
    console.err(err)
    removeUploads(uploads)
    res.status(HttpStatus.SERVICE_UNAVAILABLE).json(err)
  })
}

/**
 * Remove the uploaded files.
 *
 * @param {Object} uploads Uploaded files.
 */
const removeUploads = function (uploads) {
   // Remove the file
  for (const name in uploads) {
    const file = uploads[name].path
    fs.unlinkSync(file)
  }
}

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
