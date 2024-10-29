# emi-indo-cordova-plugin-iap
 Cordova Plugin cordova plugin In App Purchase
 
 ## Support ( Consumable | Non-consumable | Subscriptions/installments )
 ## Includes server side validation https://console.cloud.google.com 

### SDK (billing_version = 7.1.1) [Release Notes:](https://developer.android.com/google/play/billing/release-notes)

 > __Note__
> - ## It's Not a fork, it's purely rewritten, clean of 3rd party code.

> - [Code source:](https://developer.android.com/google/play/billing/integrate)

## 💰Sponsor this project
  [![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/emiindo)  
  
  
  ## Features
  
 ###  [As per original sample:](https://developer.android.com/google/play/billing/integrate)
 
- SDK Initialize a BillingClient
- Show products available to buy
- Launch the purchase flow
- Processing purchases
- Fetching purchases
- Fetching purchase history

## Features NEW Method

- getSubscriptionStatus (Free 200,000 requests per day)
- redirectToSubscriptionCenter
- redirectToSpecificSubscription
- changeSubscription



## Plugin Installation 

```sh
cordova plugin add emi-indo-cordova-plugin-iap
```
### Or
```sh
cordova plugin add https://github.com/EMI-INDO/emi-indo-cordova-plugin-iap
```
## Remove
```sh
cordova plugin rm emi-indo-cordova-plugin-iap
```

## Tabel Response Code = number

| Code | Description |
| :------- | :-------------------------------- |
| 0 | OK |
| 1 | USER_CANCELED |
| 2 | SERVICE_UNAVAILABLE |
| 3 | BILLING_UNAVAILABLE |
| 4 | ITEM_UNAVAILABLE |
| 5 | DEVELOPER_ERROR |
| 6 | ERROR |
| 7 | ITEM_ALREADY_OWNED |
| 8 | ITEM_NOT_OWNED |
| -1 | SERVICE_DISCONNECTED |
| -2 | FEATURE_NOT_SUPPORTED |
| 12 | NETWORK_ERROR |

## Initialize
```sh
cordova.plugins.emiInAppPurchase.initialize((status) => {

                 console.log("User's Play Country: " + status) 

                },
                (error) => { console.error(JSON.stringify(error)) }

)

```

## purchase Products

```sh
    cordova.plugins.emiInAppPurchase.purchaseProducts({
    
    productType: "Non-consumable", // string (Non-consumable | Consumable | Subscriptions)
    productId: "testId"
    },
    (result) => {

    const dataIsSuccess = result.isPurchasesSuccess; // boolean
    const dataProductType = result.productType; // Non-consumable | Consumable | Subscriptions
    const dataProductId = result.productsId; 
    const dataIsInstallment = result.isInstallmentPlansSupported; // boolean 

    if (dataIsSuccess && dataProductId === "testId"){

      if (dataProductType === 'Non-consumable'){

        console.log(JSON.stringify(result))

      } else if (dataProductType === 'Consumable'){

        console.log(JSON.stringify(result))

      } else if (dataProductType === 'Subscriptions'){

       if (dataIsInstallment){

         console.log(result.commitmentPaymentsCount)
         console.log(result.subsequentCommitmentPaymentsCount)

       }

       console.log(JSON.stringify(result))

    } 

    }


    },
    (error) => { // alert(error) 
   
   
   })
   ```



## getSubscriptionStatus()

- Create GCP service-account-key.json https://www.youtube.com/watch?v=gjAVd784WqE
- after the service-account.json file is downlaod,
- change the file name to service-account-key,
- place the service-account-key.json file in the www folder.



```sh

Example of Cordova project structure

my-cordova-project/
├── config.xml      
└── www/
    ├── css/            
    ├── js/         
    ├── img/        
    ├── service-account-key.json/   # File service-account  
    └── index.html  
```


> __Note__
> - You may get this error on your server side “The current user has insufficient permissions to perform the requested operation.”
- [Solution https://stackoverflow.com/questions/43536904:](https://stackoverflow.com/questions/43536904/google-play-developer-api-the-current-user-has-insufficient-permissions-to-pe/60691844#60691844)
- 
```sh


// NOTE: Essentially if all the settings are correct, you should wait 24 hours to test. getSubscriptionStatus
// Free 200,000 requests per day
cordova.plugins.emiInAppPurchase.getSubscriptionStatus({

        applicationName: "App Name",
        packageName: "com.xxxxxxx",
        purchaseToken: "token-xxxxxxx",
        subscriptionId: "id-xxxx" // productId

      },(data) => {

              
              console.log(JSON.stringify(data))

              const dataStatus = data.status; // RENEWED | CANCELED | ON_HOLD | EXPIRED | UNKNOWN
              const dataDetails = data.details;

              console.log(dataStatus)

              console.log(dataDetails.autoRenewing)
              console.log(dataDetails.countryCode)
              console.log(dataDetails.expiryTimeMillis)
              console.log(dataDetails.orderId)
              console.log(dataDetails.priceAmountMicros)
              console.log(dataDetails.priceCurrencyCode)
              console.log(dataDetails.startTimeMillis)


            },
            (error) => {

             console.error(JSON.stringify(error))

            });


```


## redirectToSubscriptionCenter()
- Use deep links to allow users to manage a subscription
```sh
cordova.plugins.emiInAppPurchase.redirectToSubscriptionCenter();
```

## redirectToSpecificSubscription()
- Link to a specific subscription management page (recommended)
```sh
cordova.plugins.emiInAppPurchase.redirectToSpecificSubscription();
```



## changeSubscription()
- Allow users to upgrade, downgrade, or change their subscription
- replacementMode string value: CHARGE_PRORATED_PRICE | CHARGE_FULL_PRICE | WITHOUT_PRORATION | DEFERRED
```sh

// https://developer.android.com/google/play/billing/subscriptions#allow-users-change


cordova.plugins.emiInAppPurchase.changeSubscription({

        oldPurchaseToken: "",
        newProductId: "",
        selectedOfferIndex: 0,
        replacementMode: "" 

      },(status) => {

              console.log(JSON.stringify(status))

            },
            (error) => {

             console.error(JSON.stringify(error))

            });


```


##  Get Product Detail

> __Note__
> - position = String
- value = ProductId | Title | Description | Item_Price | Any 

```sh
cordova.plugins.emiInAppPurchase.getProductDetail({

    productType: "Non-consumable", // string  (Non-consumable | Consumable | Subscriptions)
    productId: "id",
    position: "ProductId"
},
    (result) => { alert(result) },
    (error) => { alert(error) }
    
    )
```

##  Get Purchase History

> __Note__
> - position = String
- value = Purchase_Token | Original_Json | Quantity | Signature | Developer_Payload | Products | Purchase_Time | Any

```sh
cordova.plugins.emiInAppPurchase.getPurchaseHistory({

    productType: "Non-consumable", // string (Non-consumable | Consumable | Subscriptions)
    position: "Purchase_Token", 
    }, 
    (result) => { alert(result) },
    (error) => { alert(error) }

    )
```


##  Restore Purchases

> __Note__
> - position = String
- value = OrderId | Purchase_Token | Package_Name | Purchase_Time | Purchase_State | Quantity | Signature | Original_Json | ProductId | Any

```sh
cordova.plugins.emiInAppPurchase.restorePurchases({

    productType: "Non-consumable", // string  (Non-consumable | Consumable | Subscriptions)
    position: "OrderId", 
},
    (result) => { alert(result) },
    (error) => { alert(error) }
    
    )
```

### Support Platform ( Android )

## Coming soon Plugin ( Store user purchase data )

- Firebase Authentication
- Firebase Realtime Database

## 💰Sponsor this project
  [![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/emiindo) 
