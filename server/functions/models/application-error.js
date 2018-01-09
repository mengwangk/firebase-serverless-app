class ApplicationError {
    constructor(statusCode, error, source) {
        this.statusCode = statusCode;
        this.error = error;
        this.source = source;
    }
}

module.exports = ApplicationError;