const utils = require('../shared/utils.js');
const FieldValue = require("firebase-admin").firestore.FieldValue;

class Booking {
    constructor(name, contactNo, noOfCustomers, bookingNo = '') {
        this.id = utils.UUID.generate();
        this.name = name;
        this.contactNo = contactNo;
        this.noOfCustomers = noOfCustomers;
        this.bookedDate = Date.now();
        this.bookingNo = bookingNo;
    }
}
module.exports = Booking;