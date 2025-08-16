var exec = require('cordova/exec');

exports.initialize = function (success, error) {
    exec(success, error, 'emiInAppPurchase', 'initialize', []);
};

exports.purchaseProducts = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.productType,
        arg0.productId
    ];

    exec(success, error, 'emiInAppPurchase', 'purchaseProducts', argsArray);
};

exports.restorePurchases = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.productType,
        arg0.position
    ];

    exec(success, error, 'emiInAppPurchase', 'restorePurchases', argsArray);
};

exports.getProductDetail = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.productType,
        arg0.productId,
        arg0.position
    ];

    exec(success, error, 'emiInAppPurchase', 'getProductDetail', argsArray);
};

exports.getPurchaseHistory = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.productType,
        arg0.position
    ];
    exec(success, error, 'emiInAppPurchase', 'getPurchaseHistory', argsArray);
};



exports.redirectToSubscriptionCenter = function (success, error) {
    exec(success, error, 'emiInAppPurchase', 'redirectToSubscriptionCenter', []);
};



exports.redirectToSpecificSubscription = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.productId
    ];
    exec(success, error, 'emiInAppPurchase', 'redirectToSpecificSubscription', argsArray);
};


exports.changeSubscription = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.oldPurchaseToken,
        arg0.newProductId,
        arg0.selectedOfferIndex,
        arg0.replacementMode
    ];
    exec(success, error, 'emiInAppPurchase', 'changeSubscription', argsArray);
};




exports.getSubscriptionStatus = function (arg0, success, error) {

    if (typeof arg0 !== 'object' || Array.isArray(arg0)) {
        error("Invalid arguments. Expected an object.");
        return;
    }
    var argsArray = [
        arg0.applicationName,
        arg0.packageName,
        arg0.purchaseToken,
        arg0.subscriptionId
    ];
    exec(success, error, 'emiInAppPurchase', 'getSubscriptionStatus', argsArray);
};



