var exec = require('cordova/exec');

exports.initializeBillingClient = function (arg0, success, error) {
    exec(success, error, 'emiBillingPlugin', 'initializeBillingClient', [arg0]);
};

exports.registerProductId = function (arg0, arg1, success, error) {
    exec(success, error, 'emiBillingPlugin', 'registerProductId', [arg0, arg1]);
};

exports.purchaseItem = function (arg0, arg1, success, error) {
    exec(success, error, 'emiBillingPlugin', 'purchaseItem', [arg0, arg1]);
};

exports.restorePurchases = function (arg0, success, error) {
    exec(success, error, 'emiBillingPlugin', 'restorePurchases', [arg0]);
};

exports.getProductDetail = function (arg0, arg1, success, error) {
    exec(success, error, 'emiBillingPlugin', 'getProductDetail', [arg0, arg1]);
};

exports.getOrderProductDetail = function (arg0, arg1, success, error) {
    exec(success, error, 'emiBillingPlugin', 'getOrderProductDetail', [arg0, arg1]);
};