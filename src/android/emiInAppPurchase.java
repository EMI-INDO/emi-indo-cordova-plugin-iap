package emi.indo.cordova.plugin.iap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.GetBillingConfigParams;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.ImmutableList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by EMI INDO So on Jun 5, 2023
 */

public class emiInAppPurchase extends CordovaPlugin {
    private final String TAG = "emiInAppPurchase";
    private BillingClient billingClient;
    String ProductType = null;
    String ProductId = null;
    String Position = null;

    private CallbackContext PUBLIC_CALLBACKS = null;

    String isType = null;

    Boolean isPurchasesSuccess = false;

    private Integer commitmentPaymentsCount = null;
    private Integer subsequentCommitmentPaymentsCount = null;
    private boolean installmentPlansSupported = true;

    private String APPLICATION_NAME = null;
    private static final String KEY_FILE_PATH = "www/service-account-key.json"; // Path to your key in the assets folder
    private String PACKAGE_NAME = null;
    private String subscriptionId = null;

    protected CordovaWebView mCordovaWebView;
    // private CordovaWebView cWebView;
    private Activity mActivity;
    private Context mContext;

    private final Set<String> processedPurchases = new HashSet<>();  // To track processed purchases

    private CordovaResourceApi resourceApi;


    private final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
               // Log.d(TAG, "Response is OK");
                handlePurchase(purchase);
            }
        } else {
           // Log.d(TAG, "Response NOT OK");
            errorCallBack(billingResult);
        }
    };


    PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build();

    @Override
    public void pluginInitialize() {
        super.pluginInitialize();
        mCordovaWebView = webView;
        mActivity = this.cordova.getActivity();
        mContext = mActivity.getApplicationContext();
        resourceApi = new CordovaResourceApi(mContext, webView.getPluginManager());
    }


    @SuppressLint("LongLogTag")
    @Override
    public boolean execute(@NonNull String action, JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        PUBLIC_CALLBACKS = callbackContext;
        if (action.equals("initialize")) {
            cordova.getThreadPool().execute(this::initialize);
            return true;
        } else if (action.equals("purchaseProducts")) {
            String productType = args.getString(0);
            String productId = args.getString(1);
            try {
                this.ProductType = productType;
                this.ProductId = productId;
                cordova.getThreadPool().execute(() -> {
                    getProductType(productType);
                    purchaseProducts(productType, productId);
                });
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("restorePurchases")) {
             String productType = args.getString(0);
             String position = args.getString(1);
            try {
                this.ProductType = productType;
                this.Position = position;
                getProductType(productType);
                restoreProducts();
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("getProductDetail")) {
             String productType = args.getString(0);
             String productId = args.getString(1);
             String position = args.getString(2);
            try {
                this.ProductType = productType;
                this.ProductId = productId;
                this.Position = position;
                getProductType(productType);
                getProductDetail(PUBLIC_CALLBACKS);
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("getPurchaseHistory")) {
             String productType = args.getString(0);
             String position = args.getString(1);
            try {
                this.ProductType = productType;
                this.Position = position;
                getProductType(productType);
                getPurchaseHistory();
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("redirectToSubscriptionCenter")) {
            try {
                redirectToSubscriptionCenter();
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("redirectToSpecificSubscription")) {
            String productId = args.getString(0);
            try {
                this.ProductId = productId;
                redirectToSpecificSubscription(productId);
            } catch (Exception e) {
                PUBLIC_CALLBACKS.error(e.toString());
            }
            return true;
        } else if (action.equals("changeSubscription")) {
            String oldPurchaseToken = args.getString(0);
            String newProductId = args.getString(1);
            int selectedOfferIndex = args.getInt(2);
            String replacementMode = args.getString(3);

            ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            productList.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(newProductId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();
            // FIX Google Play Billing Library 8.0.0
            billingClient.queryProductDetailsAsync(params, (billingResult, queryResult) -> {
                List<ProductDetails> newProductDetailsList = queryResult.getProductDetailsList();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && newProductDetailsList != null && !newProductDetailsList.isEmpty()) {
                    changeSubscription(oldPurchaseToken, newProductDetailsList.get(0), selectedOfferIndex, replacementMode);
                } else {
                    handleBillingResult(billingResult);
                }
            });

            return true;
        } else if (action.equals("getSubscriptionStatus")) {
            String applicationName = args.getString(0);
            String packageName = args.getString(1);
            String purchaseToken = args.getString(2);
            String subscriptionId = args.getString(3);
            this.APPLICATION_NAME = applicationName;
            this.PACKAGE_NAME = packageName;
            this.subscriptionId = subscriptionId;
            this.getSubscriptionStatus(purchaseToken, callbackContext);
            return true;
        }

        return false;
    }








    private void getSubscriptionStatus(String purchaseToken, CallbackContext callbackContext) {
        new Thread(() -> {
            try {
                SubscriptionPurchase status = getSubscriptionStatusInternal(purchaseToken);
                if (status != null) {
                    String subscriptionStatus = getSubscriptionStatus(status);
                    JSONObject response = new JSONObject();
                    response.put("status", subscriptionStatus);
                    response.put("details", status.toPrettyString());
                    callbackContext.success(response);
                } else {
                    callbackContext.error("Subscription status is null");
                }
            } catch (GeneralSecurityException | IOException | JSONException e) {
                callbackContext.error(e.getMessage());
            }
        }).start();
    }




    private SubscriptionPurchase getSubscriptionStatusInternal(String purchaseToken) throws GeneralSecurityException, IOException {
        InputStream keyStream = mActivity.getAssets().open(KEY_FILE_PATH);

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(keyStream)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        AndroidPublisher publisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        return publisher.purchases().subscriptions()
                .get(PACKAGE_NAME, subscriptionId, purchaseToken)
                .execute();
    }





    private String getSubscriptionStatus(SubscriptionPurchase subscriptionPurchase) {
        if (subscriptionPurchase.getCancelReason() != null) {
            switch (subscriptionPurchase.getCancelReason()) {
                case 0:
                    return "RENEWED";
                case 1:
                    return "CANCELED";
                case 2:
                    return "ON_HOLD";
                case 3:
                    return "EXPIRED";
                default:
                    return "UNKNOWN";
            }
        }
        return "UNKNOWN";
    }



    private void changeSubscription(String oldPurchaseToken, ProductDetails newProductDetails, int selectedOfferIndex, String replacementMode) {
        List<ProductDetails.SubscriptionOfferDetails> offerDetailsList = newProductDetails.getSubscriptionOfferDetails();
        if (offerDetailsList != null && !offerDetailsList.isEmpty()) {
            String offerToken = offerDetailsList.get(selectedOfferIndex).getOfferToken();

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(
                            ImmutableList.of(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(newProductDetails)
                                            .setOfferToken(offerToken)
                                            .build()))
                    .setSubscriptionUpdateParams(
                            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                    .setOldPurchaseToken(oldPurchaseToken)
                                    .setSubscriptionReplacementMode(Integer.parseInt(replacementMode))
                                    .build())
                    .build();

            billingClient.launchBillingFlow(mActivity, billingFlowParams);
        } else {
             PUBLIC_CALLBACKS.error("Subscription offer details are null or empty");
        }
    }





   private void getProductType(String productType) {
        if (Objects.equals(productType, "Consumable")) {
            this.ProductType = "Consumable";
            this.isType = BillingClient.ProductType.INAPP;
        } else if (Objects.equals(productType, "Non-Consumable")) {
           this.ProductType = "Non-Consumable";
           this.isType = BillingClient.ProductType.INAPP;
       } else if (Objects.equals(productType, "Subscriptions")) {
            this.ProductType = "Subscriptions";
            this.isType = BillingClient.ProductType.SUBS;
        }
    }



    private void initBillingClient() {
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(mActivity)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases(pendingPurchasesParams)
                    .build();
        }
    }




    private void initialize() {
        initBillingClient();
        if (!billingClient.isReady()) {
            establishConnection();
        } else {

            PUBLIC_CALLBACKS.success();

        }
    }





   private void establishConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                userConfiguration();

            }

            @SuppressLint("LongLogTag")
            @Override
            public void onBillingServiceDisconnected() {
               // Log.d(TAG, "Connection NOT Established");
                establishConnection();

            }
        });
    }



    private void userConfiguration() {

        GetBillingConfigParams getBillingConfigParams = GetBillingConfigParams.newBuilder().build();


        billingClient.getBillingConfigAsync(getBillingConfigParams, (billingResult, billingConfig) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && billingConfig != null) {

                String countryCode = billingConfig.getCountryCode();
                PUBLIC_CALLBACKS.success(countryCode);
               // Log.d(TAG, "User's Play Country: " + countryCode);

            } else {

                PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
                Log.e(TAG, "Failed to get billing configuration: " + billingResult.getResponseCode());
            }
        });
    }


    private void purchaseProducts(String productType, String productId) {
        ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType.equals("Consumable") ? BillingClient.ProductType.INAPP : productType.equals("Non-Consumable") ? BillingClient.ProductType.INAPP : BillingClient.ProductType.SUBS)
                .build());
        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();
        // FIX Google Play Billing Library 8.0.0
        billingClient.queryProductDetailsAsync(params, (billingResult, queryProductDetailsResult) -> {
            List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null && !productDetailsList.isEmpty()) {
                try {
                    LaunchPurchaseFlow(productDetailsList.get(0), productType);
                } catch (Exception e) {
                    PUBLIC_CALLBACKS.error(e.toString());
                }
            } else {
                handleBillingResult(billingResult);
            }
        });
    }





    private void redirectToSubscriptionCenter() {
        String url = "https://play.google.com/store/account/subscriptions";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mActivity.startActivity(intent);
    }


    private void redirectToSpecificSubscription(String productId) {
        String packageName = mContext.getPackageName();
        String url = "https://play.google.com/store/account/subscriptions?sku=" + productId + "&package=" + packageName;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mActivity.startActivity(intent);
    }





    private void LaunchPurchaseFlow(ProductDetails productDetails, String productType) throws JSONException {
        ArrayList<BillingFlowParams.ProductDetailsParams> productList = new ArrayList<>();
        BillingFlowParams.ProductDetailsParams.Builder productDetailsParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails);

        // Handling Subscriptions
        if (productType.equals("Subscriptions")) {

            List<ProductDetails.SubscriptionOfferDetails> offerDetailsList = productDetails.getSubscriptionOfferDetails();
            if (offerDetailsList != null && !offerDetailsList.isEmpty()) {
                ProductDetails.SubscriptionOfferDetails offerDetails = offerDetailsList.get(0);
                productDetailsParamsBuilder.setOfferToken(offerDetails.getOfferToken());

                ProductDetails.InstallmentPlanDetails installmentPlanDetails = offerDetails.getInstallmentPlanDetails();

                if (installmentPlanDetails != null) {
                    // Installment plans are supported
                    commitmentPaymentsCount = installmentPlanDetails.getInstallmentPlanCommitmentPaymentsCount();
                    subsequentCommitmentPaymentsCount = installmentPlanDetails.getSubsequentInstallmentPlanCommitmentPaymentsCount();
                    installmentPlansSupported = true;
                } else {
                    // Handle countries where installment plans are not supported
                  //  Log.d(TAG, "Installment plans are not supported in this country");
                    installmentPlansSupported = false;
                }

            } else {
                PUBLIC_CALLBACKS.error("Subscription offer details are null or empty");
                return;
            }
        }

        productList.add(productDetailsParamsBuilder.build());

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productList)
                .build();

        switch (productType) {
            case "Subscriptions":
               // Log.d(TAG, "Launching billing flow for subscriptions product");
                break;
            case "Non-Consumable":
               // Log.d(TAG, "Launching billing flow for non-consumable product");
                break;
            case "Consumable":
               // Log.d(TAG, "Launching billing flow for consumable product");
                break;
        }

        billingClient.launchBillingFlow(mActivity, billingFlowParams);
    }


    private void sendPurchaseStateToJavascript(final String state) {
        ((Activity) mCordovaWebView.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String jsCode = "javascript:document.dispatchEvent(new CustomEvent('on.purchaseState', { detail: '" + state + "' }));";
                mCordovaWebView.loadUrl(jsCode);
            }
        });
    }




    private void handlePurchase(Purchase purchase) {
        try {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (ProductType.equals("Consumable")) {
                    handleConsumablePurchase(purchase);
                } else if (ProductType.equals("Subscriptions")) {
                    handleSubscriptionPurchase(purchase);
                } else {
                    handleNonConsumablePurchase(purchase);
                }

            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                // Handle pending transactions
                sendPurchaseStateToJavascript("PENDING");
                PUBLIC_CALLBACKS.error("Purchase is pending. Please complete the transaction.");

            } else {
                sendPurchaseStateToJavascript("CANCELED");
                PUBLIC_CALLBACKS.error("Purchase not completed");
            }
        } catch (Exception e) {
            PUBLIC_CALLBACKS.error("Error handling purchase: " + e.getMessage());
        }
    }





    private void handleConsumablePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();
        billingClient.consumeAsync(consumeParams, (billingResult, purchaseToken) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isPurchasesSuccess = true;
                sendPurchaseStateToJavascript("PURCHASED");
                handlePurchaseDetails(purchase);
            } else {
                handleBillingResult(billingResult);
            }
        });
    }


    private void handleSubscriptionPurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isPurchasesSuccess = true;
                    sendPurchaseStateToJavascript("PURCHASED");
                    handlePurchaseDetails(purchase);
                } else {
                    handleBillingResult(billingResult);
                }
            });
        } else {
            handlePurchaseDetails(purchase);
        }
    }



    private void handleNonConsumablePurchase(Purchase purchase) {
        if (!purchase.isAcknowledged()) {
            AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    isPurchasesSuccess = true;
                    sendPurchaseStateToJavascript("PURCHASED");
                    handlePurchaseDetails(purchase);
                } else {
                    handleBillingResult(billingResult);
                }
            });
        } else {
            handlePurchaseDetails(purchase);
        }
    }






    private void handlePurchaseDetails(Purchase purchase) {
        try {
            JSONObject result = new JSONObject();
            result.put("packageName", purchase.getPackageName());
            result.put("productsId", purchase.getProducts());
            result.put("purchaseTime", purchase.getPurchaseTime());
            result.put("orderId", purchase.getOrderId());
            result.put("purchaseToken", purchase.getPurchaseToken());
            result.put("signature", purchase.getSignature());
            result.put("purchaseState", purchase.getPurchaseState());
            result.put("originalJson", purchase.getOriginalJson());
            result.put("quantity", purchase.getQuantity());
            result.put("isAcknowledged", purchase.isAcknowledged());
            result.put("isAutoRenewing", purchase.isAutoRenewing());
            result.put("accountIdentifiers", purchase.getAccountIdentifiers());
            result.put("developerPayload", purchase.getDeveloperPayload());

            result.put("isPurchasesSuccess", isPurchasesSuccess);

            result.put("productType", ProductType);

             if (ProductType.equals("Subscriptions")) {
                // Add installment plan details if available
                if (commitmentPaymentsCount != null && subsequentCommitmentPaymentsCount != null) {
                    result.put("commitmentPaymentsCount", commitmentPaymentsCount);
                    result.put("subsequentCommitmentPaymentsCount", subsequentCommitmentPaymentsCount);
                } else if (!installmentPlansSupported) {
                    result.put("installmentPlansSupported", "Installment plans are not supported in this country");
                }
            }

            PUBLIC_CALLBACKS.success(result);
        } catch (JSONException e) {
            PUBLIC_CALLBACKS.error("Error creating JSON: " + e.getMessage());
        }
    }



    private void handleBillingResult(BillingResult billingResult) {
        int responseCode = billingResult.getResponseCode();
        // Send the response code as an integer
        if (responseCode == BillingClient.BillingResponseCode.OK) {
            PUBLIC_CALLBACKS.success(responseCode); // Send int response code
        } else {
            PUBLIC_CALLBACKS.error(responseCode); // Send int response code
        }
    }



    @SuppressLint("LongLogTag")
    private void getProductDetail(final CallbackContext PUBLIC_CALLBACKS) {
        if (isType != null) {
            ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            productList.add(QueryProductDetailsParams.Product.newBuilder().setProductId(ProductId)
                    .setProductType(isType).build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder().setProductList(productList).build();
            // FIX Google Play Billing Library 8.0.0
            billingClient.queryProductDetailsAsync(params, (billingResult, queryResult) -> {

                List<ProductDetails> productDetailsList = queryResult.getProductDetailsList();

                if (productDetailsList != null) {
                    for (ProductDetails li : productDetailsList) {
                        if (Objects.equals(Position, "ProductId")) {
                            PUBLIC_CALLBACKS.success(li.getProductId());
                        } else if (Objects.equals(Position, "Title")) {
                            PUBLIC_CALLBACKS.success(li.getTitle());
                        } else if (Objects.equals(Position, "Description")) {
                            PUBLIC_CALLBACKS.success(li.getDescription());
                        } else if (Objects.equals(Position, "item_Price")) {
                            PUBLIC_CALLBACKS
                                    .success(Objects.requireNonNull(li.getOneTimePurchaseOfferDetails()).getFormattedPrice());
                        } else if (Objects.equals(Position, "Any")) {
                            PUBLIC_CALLBACKS.success(li.toString());
                        } else {
                            PUBLIC_CALLBACKS.success(li.toString());
                        }
                        break;
                    }
                } else {
                    PUBLIC_CALLBACKS.error("Product details not found.");
                }
            });
        }
    }





private void getPurchaseHistory() {

    switch (ProductType) {
        case "Consumable":
        case "Non-Consumable":
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build(),
                    (billingResult, inAppPurchasesList) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            handleGetHistory(inAppPurchasesList);
                        } else {
                            PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
                        }
                    }
            );
            break;
        case "Subscriptions":
            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build(),
                    (billingResult, subsPurchasesList) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            handleGetHistory(subsPurchasesList);
                        } else {
                            PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
                        }
                    }
            );
            break;
        default:
           // Log.e(TAG, "Unknown ProductType: " + ProductType);
            PUBLIC_CALLBACKS.error("Unknown ProductType: " + ProductType);
            break;
    }
}




    private void handleGetHistory(List<Purchase> purchasesList) {
      //  Log.d(TAG, "handleGetHistory: Processing purchase history");
        if (purchasesList != null && !purchasesList.isEmpty()) {
            for (Purchase purchase : purchasesList) {
                switch (Position) {
                    case "Purchase_Token":
                        PUBLIC_CALLBACKS.success(purchase.getPurchaseToken());
                        break;
                    case "Original_Json":
                        PUBLIC_CALLBACKS.success(purchase.getOriginalJson());
                        break;
                    case "Quantity":
                        PUBLIC_CALLBACKS.success(purchase.getQuantity());
                        break;
                    case "Signature":
                        PUBLIC_CALLBACKS.success(purchase.getSignature());
                        break;
                    case "Developer_Payload":
                        PUBLIC_CALLBACKS.success(purchase.getDeveloperPayload());
                        break;
                    case "Products":
                        PUBLIC_CALLBACKS.success(purchase.getProducts().toString());
                        break;
                    case "Purchase_Time":
                        PUBLIC_CALLBACKS.success((int) purchase.getPurchaseTime());
                        break;
                    case "Any":
                        PUBLIC_CALLBACKS.success(purchasesList.toString());
                        break;
                    default:
                       // Log.d(TAG, "handleGetHistory: Default case hit, sending full purchase list");
                        PUBLIC_CALLBACKS.success(purchasesList.toString());
                }
            }
        } else {
           // Log.d(TAG, "handleGetHistory: No purchases found");
            PUBLIC_CALLBACKS.error("No purchases found");
        }
    }


    private void restoreProducts() {
        initBillingClient();
        if (!billingClient.isReady()) {
            establishConnection();
        } else {
            queryRestore(PUBLIC_CALLBACKS);
        }
    }



    private void queryRestore(CallbackContext PUBLIC_CALLBACKS) {
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                (billingResult, inAppPurchasesList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        handlePurchases(inAppPurchasesList, PUBLIC_CALLBACKS);
                    } else {
                        PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
                    }
                }
        );

        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                (billingResult, subsPurchasesList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        handlePurchases(subsPurchasesList, PUBLIC_CALLBACKS);
                    } else {
                        PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
                    }
                }
        );
    }






    private void handlePurchases(List<Purchase> purchasesList, CallbackContext PUBLIC_CALLBACKS) {
        if (!purchasesList.isEmpty()) {
            for (Purchase purchase : purchasesList) {
                switch (Position) {
                    case "OrderId":
                        PUBLIC_CALLBACKS.success(purchase.getOrderId());
                        break;
                    case "Purchase_Token":
                        PUBLIC_CALLBACKS.success(purchase.getPurchaseToken());
                        break;
                    case "Package_Name":
                        PUBLIC_CALLBACKS.success(purchase.getPackageName());
                        break;
                    case "Purchase_Time":
                        PUBLIC_CALLBACKS.success((int) purchase.getPurchaseTime());
                        break;
                    case "Purchase_State":
                        PUBLIC_CALLBACKS.success(purchase.getPurchaseState());
                        break;
                    case "Quantity":
                        PUBLIC_CALLBACKS.success(purchase.getQuantity());
                        break;
                    case "Signature":
                        PUBLIC_CALLBACKS.success(purchase.getSignature());
                        break;
                    case "Original_Json":
                        PUBLIC_CALLBACKS.success(purchase.getOriginalJson());
                        break;
                    case "ProductId":
                        PUBLIC_CALLBACKS.success(purchase.getProducts().toString());
                        break;
                    case "Any":
                        PUBLIC_CALLBACKS.success(purchasesList.toString());
                        break;

                }
            }
        } else {
            PUBLIC_CALLBACKS.success("No purchases found");
        }
    }





   private void errorCallBack(BillingResult billingResult) {
        PUBLIC_CALLBACKS.error(billingResult.getResponseCode());
    }




    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);

        // Query for subscription purchases
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                                    && !purchase.isAcknowledged()
                                    && !processedPurchases.contains(purchase.getPurchaseToken())) {

                                handlePurchase(purchase);
                                processedPurchases.add(purchase.getPurchaseToken());  // Mark as processed
                            }
                        }
                    } else {
                        handleBillingResult(billingResult);
                    }
                }
        );

        // Query for in-app (consumable and non-consumable) purchases
        billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                (billingResult, purchases) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (Purchase purchase : purchases) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED
                                    && !purchase.isAcknowledged()
                                    && !processedPurchases.contains(purchase.getPurchaseToken())) {

                                handlePurchase(purchase);
                                processedPurchases.add(purchase.getPurchaseToken());  // Mark as processed
                            }
                        }
                    } else {
                        handleBillingResult(billingResult);
                    }
                }
        );
    }



    @Override
    public void onDestroy() {
        if (billingClient != null) {
            billingClient.endConnection();
            billingClient = null;
        }
        super.onDestroy();
    }


}