# emi-indo-cordova-plugin-iap
 Cordova Plugin cordova plugin In App Purchase
 
 ## Support ( Consumable | Non-consumable | Subscriptions )

### SDK (billing_version = 6.0.0) [Release Notes:](https://developer.android.com/google/play/billing/release-notes)

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

## All successful or error callback responses = number

| Response Code | Description                  |
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
cordova.plugins.emiInAppPurchase.initialize(

(result) => { 

  alert(result) // = 0
  
},
 (error) => { 
 
 alert(error) // = Check the table

)

```

## purchase Products
> __Note__
> - isConsumable = boolean
- if true (consumable) Products can be bought back, productType must be Non-consumable
- if false Product cannot be repurchased, productType: Non-consumable | Subscriptions


    cordova.plugins.emiInAppPurchase.purchaseProducts(
    
    productType = "Non-consumable", // string (Non-consumable | Subscriptions)
    productId = "id",
    isConsumable = true, // boolean
    title = "title",
    description = "description",
    
    (result) => { 
    
    if (result === 0){
    console.log("Give content to the user.")
    }
    alert(result) // = 0
    }
    },
    (error) => { 
    // 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | -1 | -2 | 12 
   
     switch (error) {
      case 1:
      console.log("Check the table Description")
      break
      case 2:
      console.log("Check the table Description")
      break
      case 7:
      console.log("Call Restore purchase")
      break
      // and so on
     }
    // alert(error)
    
    })
   
  
