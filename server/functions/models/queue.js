'use strict'

const uuidv4 = require('uuid/v4')
const utils = require('../shared/utils')
/**
 * Queue.
 * @public
 * @class
 */
class Queue {
  constructor (name, minCapacity = 1, maxCapacity = 1, prefix = '', id = '', counter = 0) {
    (!id) ? this.id = uuidv4() : this.id = id
    this.name = name
    this.minCapacity = minCapacity
    this.maxCapacity = maxCapacity
    this.counter = counter;
    (prefix) ? this.prefix = prefix : this.prefix = utils.Counter.prefix()
  }
}
module.exports = Queue
