package com.jai.subscription

/**
 * Created by JAI on 01,October,2019
 */
data class PurchaseData(val packageName :String,val acknowledged:Boolean,val orderId:String,val productId:String,val developerPayload:String,val purchaseToken:String)

