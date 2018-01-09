const utils = require('../shared/utils.js');
const FieldValue = require("firebase-admin").firestore.FieldValue;

class Booking {
    constructor(name, contactNo, noOfCustomers) {
        this.id = utils.UUID.generate();
        this.name = name;
        this.contactNo = contactNo;
        this.noOfCustomers = noOfCustomers;
        this.bookedDate = Date.now();
    }
}
module.exports = Booking;