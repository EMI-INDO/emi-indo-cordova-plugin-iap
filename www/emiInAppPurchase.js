var exec = require('cordova/exec');

exports.initialize = function (arg0, success, error) {
    exec(success, error, 'emiInAppPurchase', 'initialize', [arg0]);
};

exports.purchaseNonConsumable = function (arg0, arg1, arg2, arg3, arg4, success, error) {
    exec(success, error, 'emiInAppPurchase', 'purchaseNonConsumable', [arg0, arg1, arg2, arg3, arg4]);
};

exports.restoreNonConsumable = function (arg0, arg1, success, error) {
    exec(success, error, 'emiInAppPurchase', 'restoreNonConsumable', [arg0, arg1]);
};

exports.getProductDetail = function (arg0, arg1, arg2, success, error) {
    exec(success, error, 'emiInAppPurchase', 'getProductDetail', [arg0, arg1, arg2]);
};

exports.getPurchaseHistory = function (arg0, arg1, success, error) {
    exec(success, error, 'emiInAppPurchase', 'getPurchaseHistory', [arg0, arg1]);
};
