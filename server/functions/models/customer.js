const utils = require('../shared/utils.js');

class Customer {
    constructor(name, contactNo) {
        this.id = utils.UUID.generate();
        this.name = name;
        this.contactNo = contactNo;
    }
}
module.exports = Customer;