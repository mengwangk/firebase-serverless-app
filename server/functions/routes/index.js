const express = require('express');
const path = require('path');
const constants = require('../shared/constants');

const router = express.Router();

router.get('/', function (req, res, next) {
    res.render('index', { appName: constants.AppName, partials: Object.assign({}, constants.partials)});
});

module.exports = router;