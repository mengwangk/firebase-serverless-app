'use strict'

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
      if (source[key]) target[key] = source[key]
    }
  }

  return self
})()

module.exports = {
  Counter: Counter,
  Mapper: Mapper
}
