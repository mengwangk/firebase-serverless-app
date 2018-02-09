'use strict'

const History = require('./history')

/**
 * Archived booking.
 *
 * @public
 * @class
 */
class Archive extends History {
  /**
   * Constructor.
   *
   * @param {string} status Status - Archived.
   * @param {Object} history History object.
   * @param {string} queueName Queue name.
   */
  constructor (status, history, queueName) {
    super(history.queueId, queueName, status, history, history.historyDate)
  }
}
module.exports = Archive
