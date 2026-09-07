package com.factory.innerlogaimoodjournal.billing

import android.content.Context
import app.cash.turbine.test
import com.android.billingclient.api.Purchase
import com.factory.innerlogaimoodjournal.util.FakeSharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumManagerTest {

    private val context: Context = mockk()

    private fun purchase(state: Int, products: List<String>, acknowledged: Boolean = true): Purchase =
        mockk {
            every { purchaseState } returns state
            every { this@mockk.products } returns products
            every { isAcknowledged } returns acknowledged
        }

    @Test
    fun `isPremium starts false when nothing persisted`() {
        val manager = PremiumManager(context, FakeSharedPreferences())
        assertFalse(manager.isPremium.value)
        assertFalse(manager.hasSupportPack.value)
    }

    @Test
    fun `isPremium reflects value already persisted from a previous session`() {
        val prefs = FakeSharedPreferences(mapOf("is_premium" to true))
        val manager = PremiumManager(context, prefs)
        assertTrue(manager.isPremium.value)
    }

    @Test
    fun `active subscription purchase marks user premium and persists it`() {
        val prefs = FakeSharedPreferences()
        val manager = PremiumManager(context, prefs)

        manager.updateFromPurchases(
            listOf(purchase(Purchase.PurchaseState.PURCHASED, listOf(BillingProducts.SUBSCRIPTION_YEARLY)))
        )

        assertTrue(manager.isPremium.value)
        assertTrue(prefs.getBoolean("is_premium", false))
    }

    @Test
    fun `persisted premium state survives across manager instances`() {
        val prefs = FakeSharedPreferences()
        val first = PremiumManager(context, prefs)
        first.updateFromPurchases(
            listOf(purchase(Purchase.PurchaseState.PURCHASED, listOf(BillingProducts.SUBSCRIPTION_LIFETIME)))
        )

        val second = PremiumManager(context, prefs)

        assertTrue(second.isPremium.value)
    }

    @Test
    fun `losing all active entitlements revokes premium status`() = kotlinx.coroutines.test.runTest {
        val prefs = FakeSharedPreferences(mapOf("is_premium" to true))
        val manager = PremiumManager(context, prefs)

        manager.isPremium.test {
            assertTrue(awaitItem())
            manager.updateFromPurchases(emptyList())
            assertFalse(awaitItem())
        }
        assertFalse(prefs.getBoolean("is_premium", true))
    }

    @Test
    fun `pending purchase does not grant premium`() {
        val manager = PremiumManager(context, FakeSharedPreferences())

        manager.updateFromPurchases(
            listOf(purchase(Purchase.PurchaseState.PENDING, listOf(BillingProducts.SUBSCRIPTION_YEARLY)))
        )

        assertFalse(manager.isPremium.value)
    }

    @Test
    fun `unrelated product purchase does not grant premium`() {
        val manager = PremiumManager(context, FakeSharedPreferences())

        manager.updateFromPurchases(
            listOf(purchase(Purchase.PurchaseState.PURCHASED, listOf("some.other.sku")))
        )

        assertFalse(manager.isPremium.value)
    }

    @Test
    fun `support pack purchase is tracked independently of premium`() {
        val manager = PremiumManager(context, FakeSharedPreferences())

        manager.updateFromPurchases(
            listOf(purchase(Purchase.PurchaseState.PURCHASED, listOf(BillingProducts.IAP_SMALL)))
        )

        assertTrue(manager.hasSupportPack.value)
        assertFalse(manager.isPremium.value)
    }

    @Test
    fun `does not re-emit isPremium when status is unchanged`() = kotlinx.coroutines.test.runTest {
        val manager = PremiumManager(context, FakeSharedPreferences())

        manager.isPremium.test {
            assertFalse(awaitItem())
            manager.updateFromPurchases(emptyList())
            expectNoEvents()
        }
    }

    @Test
    fun `hasSeenPaywall defaults to false and flips after markPaywallSeen`() {
        val manager = PremiumManager(context, FakeSharedPreferences())

        assertFalse(manager.hasSeenPaywall())
        manager.markPaywallSeen()
        assertTrue(manager.hasSeenPaywall())
    }
}
