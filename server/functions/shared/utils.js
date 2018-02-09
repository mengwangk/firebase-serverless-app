'use strict'

const config = require('../env.json')[process.env.NODE_ENV || 'development']
const constants = require('./constants')

/**
 * Queue counter helper.
 * @public
 */
const Counter = (function () {
  var self = {}
  var chars = 'ABCDEFGHIJKLMNOPQURSTUVWXYZ'

  /**
   * Generate a random prefix between A-Z.
   * @public
   */
  self.prefix = function () {
    return chars.substr(Math.floor(Math.random() * chars.length - 2), 1)
  }

  /**
   * Check the queue number. Reset to 1 if necessary.
   *
   * @param {number} counter Current queue number.
   * @returns {number} Counter
   * @public
   */
  self.next = function (counter) {
    if (counter >= 999) return 1
    return counter
  }

  return self
})()

/**
 * Object mapper helper.
 * @public
 */
const Mapper = (function () {
  var self = {}

  /**
   * Map fields from source to target object.
   *
   * @param {Object} target Target object
   * @param {Object} source Source object
   */
  self.assign = function (target, source) {
    if (!target || !source) return

    // Map the field values
    for (let [key, value] of Object.entries(target)) {
      // if (source[key]) target[key] = source[key]
      if (source.hasOwnProperty(key)) target[key] = source[key]
    }
  }

  return self
})()

/**
 * Upload helper.
 * @public
 */
const Upload = (function () {
  var self = {}

  /**
   * Check if file size has exceeded the limit.
   *
   * @param {number} size File size in KB.
   */
  self.hasExceedMaxAllowedSize = function (size) {
    return (size / 1024) >= constants.MaxFileSize
  }

  /**
   * Check if file type is allowed
   *
   * @param {string} size File size in KB.
   */
  self.isFileTypeAllowed = function (name) {
    return name.match(constants.AllowedImageTypes)
  }

  /**
   * Create the storage reference.
   *
   * @param {Object} entity Entity.
   * @param {string} fileName File name.
   */
  self.createStoragePath = function (entity, fileName) {
    return '/' + config.app_name + '/' + entity.id + '/' + fileName
  }

  return self
})()

module.exports = {
  Counter: Counter,
  Mapper: Mapper,
  Upload: Upload
}
