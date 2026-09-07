package com.factory.innerlogaimoodjournal.billing

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Wraps Google Play Billing Library v6. Owns the [BillingClient] connection and exposes
 * product catalog / purchase state as flows so ViewModels can observe without holding
 * a reference to the client itself.
 *
 * [billingClientFactory] defaults to building a real Play Billing client but can be
 * overridden (e.g. in tests) to supply a fake/mocked [BillingClient].
 */
class BillingManager(
    private val context: Context,
    billingClientFactory: (PurchasesUpdatedListener) -> BillingClient = { listener ->
        BillingClient.newBuilder(context)
            .setListener(listener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()
    }
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _purchases = MutableStateFlow<List<Purchase>>(emptyList())
    val purchases: StateFlow<List<Purchase>> = _purchases.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<BillingEvent> = _events.asSharedFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, updatedPurchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (updatedPurchases != null) {
                    scope.launch { handlePurchases(updatedPurchases) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _events.tryEmit(BillingEvent.PurchaseCancelled)
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.NETWORK_ERROR ->
                _events.tryEmit(BillingEvent.NetworkUnavailable)
            else ->
                _events.tryEmit(BillingEvent.PurchaseError(billingResult.debugMessage))
        }
    }

    private val billingClient: BillingClient = billingClientFactory(purchasesUpdatedListener)

    fun startConnection() {
        if (billingClient.isReady) return
        if (!isNetworkAvailable()) {
            _events.tryEmit(BillingEvent.NetworkUnavailable)
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val connected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                _isConnected.value = connected
                if (connected) {
                    scope.launch {
                        queryProductDetails()
                        queryExistingPurchases()
                    }
                } else {
                    _events.tryEmit(BillingEvent.PurchaseError(billingResult.debugMessage))
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    fun refresh() {
        if (!billingClient.isReady) {
            startConnection()
            return
        }
        scope.launch { queryExistingPurchases() }
    }

    private suspend fun queryProductDetails() {
        val subsProducts = BillingProducts.SUBSCRIPTION_PRODUCT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val oneTimeProducts = BillingProducts.ONE_TIME_PRODUCT_IDS.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val results = mutableMapOf<String, ProductDetails>()
        listOf(subsProducts, oneTimeProducts).forEach { productList ->
            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                result.productDetailsList?.forEach { details -> results[details.productId] = details }
            }
        }
        _productDetails.value = results
    }

    private suspend fun queryExistingPurchases() {
        val subs = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        )
        val inApp = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        )
        val combined = subs.purchasesList + inApp.purchasesList
        handlePurchases(combined)
    }

    private suspend fun handlePurchases(purchaseList: List<Purchase>) {
        _purchases.value = purchaseList

        val purchased = purchaseList.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        purchased.filterNot { it.isAcknowledged }.forEach { purchase ->
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            val result = billingClient.acknowledgePurchase(ackParams)
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _events.tryEmit(BillingEvent.PurchaseSuccess)
            } else {
                _events.tryEmit(BillingEvent.PurchaseError(result.debugMessage))
            }
        }
        if (purchased.isNotEmpty() && purchased.all { it.isAcknowledged }) {
            _events.tryEmit(BillingEvent.PurchaseSuccess)
        }
        if (purchaseList.any { it.purchaseState == Purchase.PurchaseState.PENDING }) {
            _events.tryEmit(BillingEvent.PurchasePending)
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        if (!isNetworkAvailable()) {
            _events.tryEmit(BillingEvent.NetworkUnavailable)
            return
        }
        if (!billingClient.isReady) {
            _events.tryEmit(BillingEvent.PurchaseError("Store connection isn't ready yet. Please try again."))
            startConnection()
            return
        }
        val details = _productDetails.value[productId]
        if (details == null) {
            _events.tryEmit(BillingEvent.PurchaseError("This product isn't available right now."))
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        if (details.productType == BillingClient.ProductType.SUBS) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                _events.tryEmit(BillingEvent.PurchaseError("No subscription offer is currently available."))
                return
            }
            productDetailsParams.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK &&
            result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED
        ) {
            _events.tryEmit(BillingEvent.PurchaseError(result.debugMessage))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}
