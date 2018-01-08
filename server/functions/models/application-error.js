class ApplicationError {
    constructor(error, source) {
       this.error = error;
       this.source = source;
    }
}

module.exports = ApplicationError;