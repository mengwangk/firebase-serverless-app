'use strict'

if (!Object.entries) {
  Object.entries = function (obj) {
    var ownProps = Object.keys(obj)
    var i = ownProps.length
    var resArray = new Array(i) // preallocate the Array
    while (i--) { resArray[i] = [ownProps[i], obj[ownProps[i]]] }

    return resArray
  }
}

/**
 * Define the format method on String prototype
 * @public
 */
if (!String.prototype.format) {
  String.prototype.format = function () {
    var args = arguments
    return this.replace(/{(\d+)}/g, function (match, number) {
      return typeof args[number] !== 'undefined'
          ? args[number]
          : ''
    })
  }
}

/**
 * Define the pad method on Number prototype
 * @public
 */
if (!Number.prototype.pad) {
  Number.prototype.pad = function (size) {
    var s = String(this)
    while (s.length < (size || 2)) { s = '0' + s }
    return s
  }
}

/**
 * Define name value property.
 *
 * @param {string} name Name
 * @param {string} value Value
 */
function define (name, value) {
  Object.defineProperty(exports, name, {
    value: value,
    enumerable: true
  })
}

/**
 * Handlebars partials.
 * @constant
 */
const PARTIALS = {
  meta: 'partials/meta',
  header: 'partials/header',
  footer: 'partials/footer'
}

/**
 * Possible actions for active bookings.
 */
const BOOKING_ACTION = {
  done: 'done',
  remove: 'remove'
}

/**
 * Possible actions for historical bookings.
 */
const HISTORY_ACTION = {
  return: 'return',
  archive: 'archive'
}

/**
 * Possible booking status.
 */
const BOOKING_STATUS = {
  removed: 'Removed',
  done: 'Done',
  archived: 'Archived'
}

/**
 * Application name.
 * @constant
 */
const APP_NAME = 'kyoala'

/**
 * Entity collection in Firestore.
 * @constant
 */
const ENTITY_COLLECTION = 'entity'

/**
 * Queue collection in Firestore.
 * @constant
 */
const QUEUE_COLLECTION = 'queue'

/**
 * History collection in Firestore.
 * @constant
 */
const HISTORY_COLLECTION = 'history'

/**
 * Archive collection in Firestore.
 * @constant
 */
const ARCHIVE_COLLECTION = 'archive'

/**
 * Lookup collection in Firestore.
 * @constant
 */
const LOOKUP_COLLECTION = 'lookup'

// Application constants
define('Partials', PARTIALS)
define('AppName', APP_NAME)
define('BookingAction', BOOKING_ACTION)
define('HistoryAction', HISTORY_ACTION)
define('BookingStatus', BOOKING_STATUS)
define('EntityCollection', ENTITY_COLLECTION)
define('QueueCollection', QUEUE_COLLECTION)
define('LookupCollection', LOOKUP_COLLECTION)
define('HistoryCollection', HISTORY_COLLECTION)
define('ArchiveCollection', ARCHIVE_COLLECTION)

// Server error messages
define('ServerError', 'Server error')
define('InvalidData', 'Invalid request')
define('NoRecordFound', 'No record found')
define('QueueNotFound', 'Queue does not exist')
define('BookingDeleted', 'Booking deleted')
define('QueueDeleted', 'Queue deleted')
define('HistoryUpdated', 'History updated')
define('Unauthorized', 'Unauthorized request')
define('BatchQueueClear', 'Clear queue request submitted')
define('BatchArchive', 'Archive request submitted')
