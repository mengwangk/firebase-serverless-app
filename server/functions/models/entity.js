const utils = require('../shared/utils.js');

class Entity {
    constructor(name) {
        this.id = utils.UUID.generate();
        this.name = name;
    }
}
module.exports = Entity;