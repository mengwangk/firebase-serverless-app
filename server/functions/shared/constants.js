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

define("Partials", PARTIALS);
define("AppName", 'kyoala');

// Error message
define("InvalidData", "Invalid input");
define("ServerError", "Server error");