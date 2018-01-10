const utils = require('../shared/utils.js');

class Queue {
    constructor(name, capacity = 1, prefix = '') {
        this.id = utils.UUID.generate();
        this.name = name;
        this.capacity = capacity;
        this.counter = 0;
        (this.prefix != null) ? this.prefix = prefix :  this.prefix = utils.Prefix.generate();
    }
}
module.exports = Queue;