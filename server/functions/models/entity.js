const utils = require('../shared/utils.js');

class Entity {

    constructor(name, email = '') {
        this.id = utils.UUID.generate();
        this.name = name;
        this.email = email;
    }
}
module.exports = Entity;