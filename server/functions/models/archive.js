'use strict'

const History = require('./history')

/**
 * Archived booking.
 *
 * @public
 * @class
 */
class Archive extends History {
  constructor (status, history) {
    super(history.queueId, status, history, history.historyDate)
  }
}
module.exports = Archive
