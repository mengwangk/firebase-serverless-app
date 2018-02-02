'use strict'

const uuidv4 = require('uuid/v4')

/**
 * Archive summary.
 *
 * @public
 * @class
 */
class ArchiveSummary {
  /**
   * From date field name.
   * @public
   */
  static get FROM_DATE_FIELD () {
    return 'fromDate'
  }

  /**
   * Constructor.
   *
   * @param {number} fromDate From date.
   * @param {number} toDate To date.
   * @param {number} totalBookings Total bookings for the period.
   * @param {number} id Unique id.
   */
  constructor (fromDate, toDate, totalBookings, id = '') {
    (!id) ? this.id = uuidv4() : this.id = id
    this.fromDate = fromDate
    this.toDate = toDate
    this.totalBookings = totalBookings
  }
}

module.exports = ArchiveSummary
