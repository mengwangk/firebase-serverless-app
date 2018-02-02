'use strict'

const Booking = require('./booking')

/**
 * Historical booking.
 *
 * @public
 * @class
 */
class History extends Booking {
  /**
   * History date field name.
   * @public
   */
  static get HISTORY_DATE_FIELD () {
    return 'historyDate'
  }

  /**
   * Queue id field name.
   * @public
   */
  static get QUEUE_ID_FIELD () {
    return 'queueId'
  }

  /**
   * Constructor.
   * @param {string} queueId Queue id.
   * @param {string} status Status - Removed or Done.
   * @param {Object} booking Booking instance.
   * @param {number} Timestamp.
   */
  constructor (queueId, status, booking, historyDate = Date.now()) {
    super(booking.name, booking.contactNo, booking.noOfSeats, booking.id, booking.bookingNo, booking.bookedDate)
    this.status = status
    this.queueId = queueId
    this.historyDate = historyDate
  }
}
module.exports = History
