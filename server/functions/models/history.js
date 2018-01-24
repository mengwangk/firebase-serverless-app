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
   *
   * @param {string} queueId Queue id.
   * @param {string} status Status - Removed or Done.
   * @param {Object} booking Booking instance.
   */
  constructor (queueId, status, booking) {
    super(booking.name, booking.contactNo, booking.noOfSeats, booking.id, booking.bookingNo, booking.bookedDate)
    this.status = status
    this.queueId = queueId
  }
}
module.exports = History
