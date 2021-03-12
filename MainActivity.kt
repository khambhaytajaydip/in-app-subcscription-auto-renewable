package com.jai.subscription

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

/**
 * use this link for upload sign apk in alpha/beta testing, add testers etc..
 * @author https://developer.android.com/google/play/billing/billing_subscriptions
 */

class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {
    lateinit private var billingClient: BillingClient
    var skuDetailList: List<SkuDetails> = listOf()
    private lateinit var productsAdapter: ProductAdapter
    lateinit var tvPriceText: TextView
    lateinit var tvPriceDetail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
	    * Setup Billing detail from playstore
	    */
	    setupBillingClient()
        val btnUpgrade = findViewById<Button>(R.id.btnUpgrade)
        tvPriceText = findViewById(R.id.textView)
        tvPriceDetail = findViewById(R.id.textView10)


        btnUpgrade.setOnClickListener {
            /**
             * start purchase
             */
            if (skuDetailList != null && skuDetailList.size > 0) {
                launchBillingFLow(skuDetailList)
            }

        }
    }

    /**
     * START CONNECTION WITH GOOGLE PLAY STORE
     */

    private fun setupBillingClient() {


        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("jai", "BILLING : onBillingSetupFinished() response: " + billingResult)
                    // The BillingClient is ready. You can query purchases here.
                }
            }

            override fun onBillingServiceDisconnected() {
                Toast.makeText(
                    this@MainActivity,
                    "Try to restart the connection on the next request to",
                    Toast.LENGTH_SHORT
                )
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })


        /**
         * GET LIST OF ALL PRODUCT ADDED
         */
        Handler().postDelayed({  
            if (billingClient.isReady) {
                val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(skuList)
                    .setType(BillingClient.SkuType.SUBS)
                    .build()

                billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                    if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(
                            "jai",
                            "querySkuDetailsAsync, responseCode: $responseCode  \n " + skuDetailsList
                        )
                        if (skuDetailsList != null && skuDetailsList.size > 0)
                            setupPriceToTextView(skuDetailsList)

                        skuDetailList = skuDetailsList
//                        initProductAdapter(skuDetailsList)
                    } else {
                        Log.d("jai", "Can't querySkuDetailsAsync, responseCode: $responseCode")
                    }
                }
            } else {
                Log.d("jai", "Billing Client not ready")
            }
        }, 200)
    }


    /**
     * SETUP PRICE BASED ON COUNTRY TO TEXTVIEW OR LIST IN DIALOG ETC
     */

    private fun setupPriceToTextView(skuDetailsList: List<SkuDetails>) {
        tvPriceText.text = tvPriceText.text.toString().replace("$3.99", skuDetailsList[0].price)
        tvPriceText.visibility = View.VISIBLE
        tvPriceDetail.text = tvPriceDetail.text.toString().replace("$3.99", skuDetailsList[0].price)
        tvPriceDetail.visibility =View.VISIBLE
    }

    companion object {

        private val skuList = listOf("skuid")  // add product id/skuid here

    }

    /**
     * START PAYMENT
     */
    private fun launchBillingFLow(skuType: List<SkuDetails>) {
        var flowParams: BillingFlowParams? =
            BillingFlowParams.newBuilder()
                .setSkuDetails(skuType[0])
                .build()
        val billingResponseCode = billingClient.launchBillingFlow(this, flowParams)
        if (billingResponseCode.responseCode == BillingClient.BillingResponseCode.OK) {
            // do something you want
            Log.d("jai", "response done :")

        }


    }


    private fun initProductAdapter(skuDetailsList: List<SkuDetails>) {
        productsAdapter = ProductAdapter(skuDetailsList) {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(this, billingFlowParams)
        }
        products.layoutManager = LinearLayoutManager(this)
        products.adapter = productsAdapter
    }


    /**
     * PAYMENT SUCCESS CALL THIS METHOD
     */
    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            val purchaseData =
                Gson().fromJson(purchases[0].originalJson.toString(), PurchaseData::class.java)
            purchaseData.purchaseToken
		
       
            Log.d("jai", "urchaseData succcess : " + purchaseData)
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchaseData.acknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchaseData.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(
                    acknowledgePurchaseParams
                ) {

                }
            }

        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("jai", "USER CANCEL BILLING")

        } else {

        }



    }


}
