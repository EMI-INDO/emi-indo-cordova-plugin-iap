package emi.indo.cordova.plugin.iap;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class echoes a string called from JavaScript.
 */
public class emiBillingPlugin extends CordovaPlugin {
    private final String TAG = "emiBillingPlugin";

    private final CallbackContext PUBLIC_CALLBACKS = null;
   public CordovaWebView cWebView;

   String ProductId = null;
   String Type = null;

   String Position = null;

    BillingClient billingClient;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cWebView = webView;
    }

    @Override
    public boolean execute(@NonNull String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("initializeBillingClient")) {

            billingClient = BillingClient.newBuilder(cordova.getActivity())
                    .enablePendingPurchases()
                    .setListener(
                            (billingResult, list) -> {

                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                    for (Purchase purchase : list) {

                                      //  Log.d(TAG, "Response is OK");
                                        handlePurchase(purchase, callbackContext);
                                    }
                                } else {

                                    Log.d(TAG, "Response NOT OK");
                                    callbackContext.error(billingResult.getResponseCode());
                                }
                            }
                    ).build();

            establishConnection(callbackContext);


            return true;
        }

        if (action.equals("registerProductId")) {
            final String productId = args.getString(0);
            final String type = args.getString(1);

            try {
                ProductId = productId;
                Type = type;
            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

            _registerProductId(callbackContext);

            return true;
        }

        if (action.equals("purchaseItem")) {
           final String productId = args.getString(0);
           final String type = args.getString(1);

            try {
                ProductId = productId;
                Type = type;
            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

            _purchaseItem();

            return true;
        }

        if (action.equals("restorePurchases")) {

            final String productId = args.getString(0);

            try {
                ProductId = productId;

            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

           restorePurchases(callbackContext);

            return true;
        }

        if (action.equals("getProductDetail")) {

            final String productId = args.getString(0);
            final String type = args.getString(1);

            try {
                ProductId = productId;
                Type = type;
            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

            getProductDetail(callbackContext);

            return true;
        }


        if (action.equals("getOrderProductDetail")) {

            final String productId = args.getString(0);
            final String position = args.getString(1);

            try {
                ProductId = productId;
                Position = position;
            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

            getOrderProductDetail(callbackContext);

            return true;
        }




        return false;
    }

    private void _registerProductId(CallbackContext callbackContext) {

        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        final ArrayList<String> purchaseItemIDs = new ArrayList<String>() {{
            add(ProductId);
            add(Type);

        }};

        for (String ids : purchaseItemIDs) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ids)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {

            for (ProductDetails li : list) {
                if (li != null){

                    PluginResult result = new PluginResult(PluginResult.Status.OK, "onRegisterProductIdSuccess");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);
                    callbackContext.success(li.getProductId());

                } else {

                    PluginResult result = new PluginResult(PluginResult.Status.OK, "onRegisterProductIdError");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);

                }
            }


        });

    }


    void _purchaseItem() {


        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        final ArrayList<String> purchaseItemIDs = new ArrayList<String>() {{
            add(ProductId);
            add(Type);

        }};

        //Set your In App Product ID in setProductId()
        for (String ids : purchaseItemIDs) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ids)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {

                LaunchPurchaseFlow(list.get(0));

        });
    }

    void LaunchPurchaseFlow(ProductDetails productDetails) {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();

        productList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        billingClient.launchBillingFlow(cordova.getActivity(), billingFlowParams);
    }

    private void getOrderProductDetail(CallbackContext callbackContext) {


            billingClient = BillingClient.newBuilder(cordova.getActivity()).enablePendingPurchases().setListener((billingResult, list) -> {
            }).build();
            final BillingClient finalBillingClient = billingClient;
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingServiceDisconnected() {
                    establishConnection(callbackContext);
                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        finalBillingClient.queryPurchasesAsync(
                                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), (billingResult1, list) -> {
                                    if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        if (list.size() > 0) {

                                            Log.d("TAG", "IN APP SUCCESS RESTORE: " + list);

                                            for (int i = 0; i < list.size(); i++) {

                                                if (list.get(i).getProducts().contains(ProductId)) {
                                                    if (Objects.equals(Position, "orderId")) {
                                                        callbackContext.success(list.get(i).getOrderId());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "purchaseToken")) {
                                                        callbackContext.success(list.get(i).getPurchaseToken());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "packageName")) {
                                                        callbackContext.success(list.get(i).getPackageName());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "purchaseTime")) {
                                                        callbackContext.success((int) list.get(i).getPurchaseTime());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "purchaseState")) {
                                                        callbackContext.success(list.get(i).getPurchaseState());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "quantity")) {
                                                        callbackContext.success(list.get(i).getQuantity());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "signature")) {
                                                        callbackContext.success(list.get(i).getSignature());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "originalJson")) {
                                                        callbackContext.success(list.get(i).getOriginalJson());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else if (Objects.equals(Position, "productId")) {
                                                        callbackContext.success(list.get(i).getProducts().toString());
                                                        _getOrderProductDetail(callbackContext);
                                                    } else {

                                                        callbackContext.success(list.get(i).getOriginalJson());

                                                    }

                                                    _getOrderProductDetail(callbackContext);

                                                }

                                            }
                                        } else {

                                            callbackContext.error(billingResult.getResponseCode());
                                        }
                                    } else {

                                        callbackContext.error(billingResult.getResponseCode());
                                    }
                                });
                    } else {

                        callbackContext.error(billingResult.getResponseCode());
                    }
                }
            });

    }

    private void _getOrderProductDetail(CallbackContext callbackContext) {

        PluginResult result = new PluginResult(PluginResult.Status.OK, "onGetOrderProductDetailSuccess");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);

    }

    private void restorePurchases(CallbackContext callbackContext) {

        billingClient = BillingClient.newBuilder(cordova.getActivity()).enablePendingPurchases().setListener((billingResult, list) -> {
        }).build();
        final BillingClient finalBillingClient = billingClient;
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {

                establishConnection(callbackContext);
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), (billingResult1, list) -> {
                                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                    if (list.size() > 0) {

                                        Log.d("TAG", "IN APP SUCCESS RESTORE: " + list);

                                        for (int i = 0; i < list.size(); i++) {

                                            if (list.get(i).getProducts().contains(ProductId)) {

                                                PluginResult result = new PluginResult(PluginResult.Status.OK, "on.Restored.Success");   //Facebook Banner AdDistroyed
                                                result.setKeepCallback(true);
                                                callbackContext.sendPluginResult(result);

                                            } else {

                                                callbackContext.error(list.toString());
                                            }

                                        }
                                    } else {

                                        Log.d("TAG", "In APP Not Found To Restore");
                                        callbackContext.error(list.toString());
                                    }
                                } else {

                                    callbackContext.error(billingResult.getResponseCode());
                                }
                            });

                } else {

                    callbackContext.error(billingResult.getResponseCode());

                }
            }
        });
    }





    void establishConnection(CallbackContext callbackContext) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {


                    PluginResult result = new PluginResult(PluginResult.Status.OK, "on.Billing.Setup.Finished");
                    result.setKeepCallback(true);
                    callbackContext.sendPluginResult(result);

                }
            }

            @Override
            public void onBillingServiceDisconnected() {


                establishConnection(callbackContext);

                PluginResult result = new PluginResult(PluginResult.Status.OK, "on.Billing.Service.Disconnected");
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);

            }
        });
    }






    void getProductDetail(CallbackContext callbackContext) throws JSONException {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

        final ArrayList<String> purchaseItemIDs = new ArrayList<String>()
        {{
            add(ProductId);
            add(Type);

        }};

        //Set your In App Product ID in setProductId()
        for (String ids : purchaseItemIDs) {
            productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(ids)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
        }

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, list) -> {

            for (ProductDetails li : list) {

                callbackContext.success(li.toString());

                PluginResult result = new PluginResult(PluginResult.Status.OK,  "on.GetProductDetail.Success");
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);

            }
            //Do Anything that you want with requested product details
        });
    }







   void handlePurchase(Purchase purchases, CallbackContext callbackContext) {
        if (!purchases.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.getPurchaseToken())
                    .build(), billingResult -> {

                if (Objects.equals(Type, "Consumable")){

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (String pur : purchases.getProducts()) {
                        if (pur.equalsIgnoreCase(ProductId)) {
                            Log.d("TAG", "Purchase is{ successful");

                                ConsumePurchase(purchases, callbackContext);
                        } else {
                            callbackContext.error(billingResult.getResponseCode());
                        }
                    }
                } else {
                    callbackContext.error(billingResult.getResponseCode());
                 }
                }

                if (Objects.equals(Type, "Non-Consumable")){

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (String pur : purchases.getProducts()) {
                            if (pur.equalsIgnoreCase(ProductId)) {

                                PluginResult result = new PluginResult(PluginResult.Status.OK, "onNon-Consumable.PurchaseSuccessful");
                                result.setKeepCallback(true);
                                callbackContext.sendPluginResult(result);
                            } else {
                                callbackContext.error(billingResult.getResponseCode());
                            }
                        }
                    } else {

                        callbackContext.error(billingResult.getResponseCode());

                    }

                }
            });
        }
    }



    //This function will be called in handlepurchase() after success of any consumeable purchase
    void ConsumePurchase(Purchase purchase, CallbackContext callbackContext) {
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(params, (billingResult, s) -> {

            Log.d("TAG", "Consuming Successful: "+s);
            PluginResult result = new PluginResult(PluginResult.Status.OK, "onConsumable.PurchaseSuccessful");
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);

        });
    }





    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, list) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : list) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                handlePurchase(purchase, PUBLIC_CALLBACKS);
                            }
                        }
                    }
                }
        );
    }





    }
