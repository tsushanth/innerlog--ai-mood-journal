package com.factory.innerlogaimoodjournal.ui.screens.paywall

import android.app.Activity
import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.billing.BillingEvent
import com.factory.innerlogaimoodjournal.billing.BillingManager
import com.factory.innerlogaimoodjournal.billing.BillingProducts
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaywallViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var billingManager: BillingManager
    private lateinit var premiumManager: PremiumManager
    private val isPremiumFlow = MutableStateFlow(false)
    private val isConnectedFlow = MutableStateFlow(false)
    private val productDetailsFlow = MutableStateFlow(emptyMap<String, com.android.billingclient.api.ProductDetails>())
    private val eventsFlow = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)

    @Before
    fun setUp() {
        billingManager = mockk(relaxed = true)
        premiumManager = mockk(relaxed = true)
        every { premiumManager.isPremium } returns isPremiumFlow
        every { billingManager.isConnected } returns isConnectedFlow
        every { billingManager.productDetails } returns productDetailsFlow
        every { billingManager.events } returns eventsFlow
    }

    private fun viewModel() = PaywallViewModel(billingManager, premiumManager)

    @Test
    fun `init marks paywall seen and starts billing connection`() {
        viewModel()

        verify { premiumManager.markPaywallSeen() }
        verify { billingManager.startConnection() }
    }

    @Test
    fun `state flows mirror billing and premium managers`() = runTest {
        val vm = viewModel()

        vm.isPremium.test { assertFalse(awaitItem()) }
        vm.isConnected.test { assertFalse(awaitItem()) }

        isConnectedFlow.value = true
        vm.isConnected.test { assertTrue(awaitItem()) }
    }

    @Test
    fun `purchase delegates to billing manager launchPurchaseFlow`() {
        val vm = viewModel()
        val activity = mockk<Activity>()

        vm.purchase(activity, BillingProducts.SUBSCRIPTION_YEARLY)

        verify { billingManager.launchPurchaseFlow(activity, BillingProducts.SUBSCRIPTION_YEARLY) }
    }

    @Test
    fun `restorePurchases delegates to billing manager refresh`() {
        val vm = viewModel()

        vm.restorePurchases()

        verify { billingManager.refresh() }
    }

    @Test
    fun `events surface billing manager events`() = runTest {
        val vm = viewModel()

        vm.events.test {
            eventsFlow.emit(BillingEvent.PurchaseSuccess)
            assertEquals(BillingEvent.PurchaseSuccess, awaitItem())
        }
    }
}
