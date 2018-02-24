'use strict'

const uuidv4 = require('uuid/v4')

/**
 * Entity.
 * @public
 * @class
 */
class Entity {
  /**
   * Email field name.
   * @public
   */
  static get EMAIL_FIELD () {
    return 'email'
  }

  /**
   * Constructor.
   *
   * @param {string} name Name.
   * @param {string} email Email.
   * @param {string} id Optional id.
   * @param {string} contactNo Optional contact no.
   * @param {string} avatar Optional avatar URL.
   * @param {string} industry Optional industry.
   * @param {string} country Optional country.
   * @param {string} address Optional address.
   * @param {string} autoAssignQueue Optional auto assign queue setting.
   * @param {string} printingDetails Optional printing details settings.
   * @public
   */
  constructor (name, email, id = '', contactNo = '', avatar = '', industry = '', country = '', address = '', autoAssignQueue = false, printingDetails = '') {
    (!id) ? this.id = uuidv4() : this.id = id
    this.name = name
    this.email = email
    this.avatar = avatar
    this.industry = industry
    this.country = country
    this.address = address
    this.contactNo = contactNo
    this.autoAssignQueue = autoAssignQueue
    this.printingDetails = printingDetails
  }
}
module.exports = Entity
