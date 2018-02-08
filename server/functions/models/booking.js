'use strict'

const uuidv4 = require('uuid/v4')
const constants = require('../shared/constants')

/**
 * Booking.
 * @public
 * @class
 */
class Booking {
  /**
   * Booked date field name.
   * @public
   */
  static get BOOKED_DATE_FIELD () {
    return 'bookedDate'
  }

  constructor (name, contactNo, noOfSeats, id = '', bookingNo = '', bookedDate = Date.now()) {
    !id ? this.id = uuidv4() : this.id = id
    !noOfSeats || noOfSeats <= 0 ? this.noOfSeats = constants.DefaultSeatQuantity : this.noOfSeats = noOfSeats
    this.name = name
    this.contactNo = contactNo
    this.bookedDate = bookedDate
    this.bookingNo = bookingNo
  }
}
module.exports = Booking
