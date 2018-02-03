'use strict'

/**
 * Application error.
 * @public
 * @class
 */
class ApplicationError extends Error {
  constructor (statusCode, error, source = '') {
    super(`${statusCode} - ${error}. Source: ${source}`)
    this.statusCode = statusCode
    this.error = error
    this.source = source
  }
}

module.exports = ApplicationError
