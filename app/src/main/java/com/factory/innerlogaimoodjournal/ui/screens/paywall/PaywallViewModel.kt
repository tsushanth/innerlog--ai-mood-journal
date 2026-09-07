package com.factory.innerlogaimoodjournal.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.factory.innerlogaimoodjournal.billing.BillingEvent
import com.factory.innerlogaimoodjournal.billing.BillingManager
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class PaywallViewModel(
    private val billingManager: BillingManager,
    private val premiumManager: PremiumManager
) : ViewModel() {

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium
    val isConnected: StateFlow<Boolean> = billingManager.isConnected
    val productDetails: StateFlow<Map<String, ProductDetails>> = billingManager.productDetails
    val events: SharedFlow<BillingEvent> = billingManager.events

    init {
        premiumManager.markPaywallSeen()
        billingManager.startConnection()
    }

    fun purchase(activity: Activity, productId: String) {
        billingManager.launchPurchaseFlow(activity, productId)
    }

    fun restorePurchases() {
        billingManager.refresh()
    }
}
