package com.factory.innerlogaimoodjournal.billing

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks whether the user currently holds a premium entitlement. State is derived from
 * [BillingManager.purchases] on every connection/refresh and cached locally so premium
 * status is available instantly on next launch, before Play has answered.
 *
 * Google Play only returns active (non-expired, non-refunded) subscriptions from
 * queryPurchasesAsync, so re-running [updateFromPurchases] on every app resume is what
 * detects subscription expiry/cancellation — no separate expiry check is needed.
 *
 * [prefs] defaults to an encrypted store but can be overridden (e.g. in tests) to avoid
 * touching the Android Keystore.
 */
class PremiumManager(
    context: Context,
    private val prefs: SharedPreferences = createDefaultPrefs(context)
) {

    private val _isPremium = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _hasSupportPack = MutableStateFlow(prefs.getBoolean(KEY_HAS_SUPPORT_PACK, false))
    val hasSupportPack: StateFlow<Boolean> = _hasSupportPack.asStateFlow()

    fun hasSeenPaywall(): Boolean = prefs.getBoolean(KEY_HAS_SEEN_PAYWALL, false)

    fun markPaywallSeen() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_PAYWALL, true).apply()
    }

    fun updateFromPurchases(purchases: List<Purchase>) {
        val active = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }

        val isPremium = active.any { purchase ->
            purchase.products.any { it in BillingProducts.PREMIUM_ENTITLEMENT_IDS }
        }
        val hasSupportPack = active.any { purchase ->
            purchase.products.any { it == BillingProducts.IAP_SMALL }
        }

        if (isPremium != _isPremium.value) {
            prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
            _isPremium.value = isPremium
        }
        if (hasSupportPack != _hasSupportPack.value) {
            prefs.edit().putBoolean(KEY_HAS_SUPPORT_PACK, hasSupportPack).apply()
            _hasSupportPack.value = hasSupportPack
        }
    }

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_HAS_SUPPORT_PACK = "has_support_pack"
        private const val KEY_HAS_SEEN_PAYWALL = "has_seen_paywall"

        private fun createDefaultPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                "innerlog_billing_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }
}
