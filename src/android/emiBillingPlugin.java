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
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class echoes a string called from JavaScript.
 */
public class emiBillingPlugin extends CordovaPlugin {
    private final String TAG = "emiBillingPlugin";
    private CordovaWebView cWebView;

   String ProductId = null;
   String Type = null;

    BillingClient billingClient;


    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        cWebView = webView;
    }

    @Override
    public boolean execute(@NonNull String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initializeBillingClient")) {

            billingClient = BillingClient.newBuilder(cordova.getActivity())
                    .enablePendingPurchases()
                    .setListener(
                            (billingResult, list) -> {

                                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                                    for (Purchase purchase : list) {

                                      //  Log.d(TAG, "Response is OK");
                                        handlePurchase(purchase);
                                    }
                                } else {

                                    cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onStartConnection');");
                                  //  Log.d(TAG, "Response NOT OK");
                                }
                            }
                    ).build();

            //start the connection after initializing the billing client
            establishConnection();


            return true;
        }

        if (action.equals("Purchase")) {
           final String productId = args.getString(0);
           final String type = args.getString(1);

            try {
                ProductId = productId;
                Type = type;
            } catch ( Exception e) {

                callbackContext.error(e.toString());
            }

            GetListsInAppDetail();

            return true;
        }

        if (action.equals("restorePurchases")) {
            // String message = args.getString(0);


           restorePurchases();

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




        return false;
    }

   

    void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query purchases here.

                    //Use any of function below to get details upon successful connection

                    // GetSingleInAppDetail();
                  //  GetListsInAppDetail(ProductId, callbackContext);

                  //  Log.d(TAG, "Connection Established");
                    cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onBillingSetupFinished');");

                }
            }

            @Override
            public void onBillingServiceDisconnected() {

                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
              // Log.d(TAG, "Connection NOT Established");

                establishConnection();
                cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onBillingServiceDisconnected');");
            }
        });
    }

    /*
     *
     * The official examples use an ImmutableList for some reason to build the query,
     * but you don't actually need to use that.
     * The setProductList method just takes List<Product> as its input, it does not require ImmutableList.
     *
     * */

    /*
     * If you have API < 24, you could just make an ArrayList instead.
     * */


    void GetListsInAppDetail() {
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


            for (ProductDetails li : list) {
              //  Log.d(TAG, "IN APP item Price" + li.getOneTimePurchaseOfferDetails().getFormattedPrice());
             //   callbackContext.success(li.toString());
                LaunchPurchaseFlow(list.get(0));
                cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onLaunchPurchaseFlow');");
            }
            //Do Anything that you want with requested product details
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
                //  Log.d(TAG, "IN APP item Price" + li.getOneTimePurchaseOfferDetails().getFormattedPrice());
                callbackContext.success(li.toString());
                cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.getProductDetail');");

            }
            //Do Anything that you want with requested product details
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



   void handlePurchase(Purchase purchases) {
        if (!purchases.isAcknowledged()) {
            billingClient.acknowledgePurchase(AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchases.getPurchaseToken())
                    .build(), billingResult -> {

                if (Objects.equals(Type, "Consumable")){

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (String pur : purchases.getProducts()) {
                        if (pur.equalsIgnoreCase(ProductId)) {
                            Log.d("TAG", "Purchase is successful");
                           // tv_status.setText("Yay! Purchased");
                                ConsumePurchase(purchases);

                            //Calling Consume to consume the current purchase
                            // so user will be able to buy same product again

                        }
                    }
                }

                //else {



                   else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.USER_CANCELED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.BILLING_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.DEVELOPER_ERROR');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ERROR');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        //    callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_ALREADY_OWNED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_NOT_OWNED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.FEATURE_NOT_SUPPORTED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_DISCONNECTED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");
                    } else {
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");

                    }



                  //  responseCode(purchases);

             //   }
                }

                if (Objects.equals(Type, "Non-Consumable")){

                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (String pur : purchases.getProducts()) {
                            if (pur.equalsIgnoreCase(ProductId)) {
                              //  Log.d("TAG", "Purchase is successful" + pur);
                                cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NonConsumable.Successful');");
                               // tv_status.setText("Yay! Purchased");
                            }
                        }
                    }

                    //else {

                      //  responseCode(purchases);



                       else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                            //  callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.USER_CANCELED');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_UNAVAILABLE');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.BILLING_UNAVAILABLE');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                            //  callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_UNAVAILABLE');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                            //  callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.DEVELOPER_ERROR');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
                            //  callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ERROR');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                            //    callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_ALREADY_OWNED');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_NOT_OWNED');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.FEATURE_NOT_SUPPORTED');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_DISCONNECTED');");
                        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                            //   callbackContext.error(billingResult.getResponseCode());
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");
                        } else {
                            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");

                        }





                  //  }


                } else {






                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.USER_CANCELED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.BILLING_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_UNAVAILABLE');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.DEVELOPER_ERROR');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
                        //  callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ERROR');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                        //    callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_ALREADY_OWNED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.ITEM_NOT_OWNED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.FEATURE_NOT_SUPPORTED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.SERVICE_DISCONNECTED');");
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.NETWORK_ERROR) {
                        //   callbackContext.error(billingResult.getResponseCode());
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");
                    } else {
                        cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.NETWORK_ERROR');");

                    }








                     //  responseCode(purchases);
                }
            });
        }
    }

    //This function will be called in handlepurchase() after success of any consumeable purchase
    void ConsumePurchase(Purchase purchase) {
        ConsumeParams params = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(params, (billingResult, s) -> {

            Log.d("TAG", "Consuming Successful: "+s);
            cWebView.loadUrl("javascript:cordova.fireDocumentEvent('onPurchase.Consumable.Successful');");
            //  tv_status.setText("Product Consumed");
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
                                handlePurchase(purchase);
                            }
                        }
                    }
                }
        );
    }





    }
