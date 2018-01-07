function define(name, value) {
    Object.defineProperty(exports, name, {
        value: value,
        enumerable: true
    });
}
const partials = {
    meta : 'partials/meta',
    header: 'partials/header',
    footer: 'partials/footer'
};

define("partials", partials);
define("AppName", 'My App Name');