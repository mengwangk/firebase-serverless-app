if (!String.prototype.format) {
    String.prototype.format = function() {
      var args = arguments;
      return this.replace(/{(\d+)}/g, function(match, number) { 
        return typeof args[number] != 'undefined'
          ? args[number]
          : ""
        ;
      });
    };
}
if (!Number.prototype.pad) {
    Number.prototype.pad = function(size) {
        var s = String(this);
        while (s.length < (size || 2)) {s = "0" + s;}
        return s;
    }
}


function define(name, value) {
    Object.defineProperty(exports, name, {
        value: value,
        enumerable: true
    });
}
const PARTIALS = {
    meta : 'partials/meta',
    header: 'partials/header',
    footer: 'partials/footer'
};

const APP_NAME = "kyoala";
const ENTITY_COLLECTION = "entity";
const QUEUE_COLLECTION = "queue";

define("Partials", PARTIALS);
define("AppName", APP_NAME);
define("EntityCollection", ENTITY_COLLECTION);
define("QueueCollection", QUEUE_COLLECTION);

// Error message
define("ServerError", "Server error");
define("InvalidData", "Invalid input");
define("NoRecordFound", "No record found");
define("BatchQueueDelete", "Queue deletion request submitted");
define("BookingDeleted", "Booking deleted");