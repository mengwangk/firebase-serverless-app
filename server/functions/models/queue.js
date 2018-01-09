const utils = require('../shared/utils.js');

class Queue {
    constructor(name) {
        this.id = utils.UUID.generate();
        this.name = name;
    }
}
module.exports = Queue;