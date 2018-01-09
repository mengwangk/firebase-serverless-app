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
