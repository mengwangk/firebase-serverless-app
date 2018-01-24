'use strict'

const Booking = require('./booking')

/**
 * Archived booking.
 *
 * @public
 * @class
 */
class Archive extends Booking {
  constructor (queueId, booking) {
    super(booking.name, booking.contactNo, booking.noOfSeats, booking.id, booking.bookingNo, booking.bookedDate)
    this.queueId = queueId
  }
}
module.exports = Archive
