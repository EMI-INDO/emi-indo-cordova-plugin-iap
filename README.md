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

```sh
    cordova.plugins.emiInAppPurchase.purchaseProducts(
    
    productType = "Non-consumable", // string (Non-consumable | Subscriptions)
    productId = "id",
    isConsumable = true, // boolean
    title = "title",
    description = "description",
    
    (result) => { 

    console.log(result.packageName)
    console.log(result.productsId)
    console.log(result.purchaseTime)
    console.log(result.orderId)
    console.log(result.purchaseToken)
    console.log(result.signature)
    console.log(result.purchaseState)
    console.log(result.originalJson)
    console.log(result.quantity)
    console.log(result.isAcknowledged)
    console.log(result.isAutoRenewing)
    console.log(result.accountIdentifiers)
    console.log(result.developerPayload)

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
   ```



##  Get Product Detail

> __Note__
> - position = String
- value = ProductId | Title | Description | Item_Price | Any 

```sh
cordova.plugins.emiInAppPurchase.getProductDetail(

    productType = "Non-consumable", // string (Non-consumable | Subscriptions)
    productId = "id",
    position = "ProductId", 
    (result) => { alert(result) },
    (error) => { alert(error)

    });
```

##  Get Purchase History

> __Note__
> - position = String
- value = Purchase_Token | Original_Json | Quantity | Signature | Developer_Payload | Products | Purchase_Time | Any

```sh
cordova.plugins.emiInAppPurchase.getPurchaseHistory(

    productType = "Non-consumable", // string (Non-consumable | Subscriptions)
    position = "Purchase_Token", 
    (result) => { alert(result) },
    (error) => { alert(error)

    });
```


##  Restore Purchases

> __Note__
> - position = String
- value = OrderId | Purchase_Token | Package_Name | Purchase_Time | Purchase_State | Quantity | Signature | Original_Json | ProductId | Any

```sh
cordova.plugins.emiInAppPurchase.restorePurchases(

    productType = "Non-consumable", // string (Non-consumable | Subscriptions)
    position = "OrderId", 
    (result) => { alert(result) },
    (error) => { alert(error)

    });
```

### Support Platform ( Android )

## Coming soon Plugin ( Store user purchase data )

- Firebase Authentication
- Firebase Realtime Database

## 💰Sponsor this project
  [![PayPal](https://img.shields.io/badge/PayPal-00457C?style=for-the-badge&logo=paypal&logoColor=white)](https://paypal.me/emiindo) 
