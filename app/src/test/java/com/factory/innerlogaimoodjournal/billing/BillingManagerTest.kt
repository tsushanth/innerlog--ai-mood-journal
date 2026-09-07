package com.factory.innerlogaimoodjournal.billing

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import app.cash.turbine.test
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.factory.innerlogaimoodjournal.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BillingManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var billingClient: BillingClient
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var capturedListener: PurchasesUpdatedListener
    private lateinit var manager: BillingManager

    private val okResult: BillingResult = mockk {
        every { responseCode } returns BillingClient.BillingResponseCode.OK
        every { debugMessage } returns ""
    }

    @Before
    fun setUp() {
        billingClient = mockk(relaxed = true)
        connectivityManager = mockk()
        context = mockk {
            every { getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        }
        setNetworkAvailable(true)

        manager = BillingManager(context) { listener ->
            capturedListener = listener
            billingClient
        }
    }

    private fun setNetworkAvailable(available: Boolean) {
        if (!available) {
            every { connectivityManager.activeNetwork } returns null
            return
        }
        val network = mockk<Network>()
        val capabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns capabilities
        every { capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
    }

    private fun stubSuccessfulConnection(productDetails: List<ProductDetails> = listOf(mockk(relaxed = true))) {
        every { billingClient.isReady } returns false
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { billingClient.startConnection(capture(stateListenerSlot)) } answers {
            every { billingClient.isReady } returns true
            stateListenerSlot.captured.onBillingSetupFinished(okResult)
        }
        every { billingClient.queryProductDetailsAsync(any<QueryProductDetailsParams>(), any()) } answers {
            secondArg<ProductDetailsResponseListener>().onProductDetailsResponse(okResult, productDetails)
        }
        every { billingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any<PurchasesResponseListener>()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(okResult, emptyList())
        }
    }

    @Test
    fun `startConnection loads product details and marks connected on success`() = runTest {
        val product = mockk<ProductDetails>(relaxed = true) { every { productId } returns BillingProducts.SUBSCRIPTION_YEARLY }
        stubSuccessfulConnection(listOf(product))

        manager.isConnected.test {
            assertFalse(awaitItem())
            manager.startConnection()
            assertTrue(awaitItem())
        }

        assertEquals(product, manager.productDetails.value[BillingProducts.SUBSCRIPTION_YEARLY])
    }

    @Test
    fun `startConnection failure emits PurchaseError and leaves disconnected`() = runTest {
        val errorResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
            every { debugMessage } returns "unavailable"
        }
        val stateListenerSlot = slot<BillingClientStateListener>()
        every { billingClient.isReady } returns false
        every { billingClient.startConnection(capture(stateListenerSlot)) } answers {
            stateListenerSlot.captured.onBillingSetupFinished(errorResult)
        }

        manager.events.test {
            manager.startConnection()
            assertEquals(BillingEvent.PurchaseError("unavailable"), awaitItem())
        }
        assertFalse(manager.isConnected.value)
    }

    @Test
    fun `startConnection without network emits NetworkUnavailable and does not connect`() = runTest {
        setNetworkAvailable(false)

        manager.events.test {
            manager.startConnection()
            assertEquals(BillingEvent.NetworkUnavailable, awaitItem())
        }
        verify(exactly = 0) { billingClient.startConnection(any()) }
    }

    @Test
    fun `purchases update with acknowledged purchase emits PurchaseSuccess`() = runTest {
        stubSuccessfulConnection()
        manager.startConnection()

        val purchase = mockk<Purchase>(relaxed = true) {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns false
            every { purchaseToken } returns "token-1"
        }
        every { billingClient.acknowledgePurchase(any<AcknowledgePurchaseParams>(), any()) } answers {
            secondArg<AcknowledgePurchaseResponseListener>().onAcknowledgePurchaseResponse(okResult)
        }

        manager.events.test {
            capturedListener.onPurchasesUpdated(okResult, listOf(purchase))
            assertEquals(BillingEvent.PurchaseSuccess, awaitItem())
        }
        assertEquals(listOf(purchase), manager.purchases.value)
    }

    @Test
    fun `purchases update cancelled by user emits PurchaseCancelled`() = runTest {
        val cancelledResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.USER_CANCELED
        }

        manager.events.test {
            capturedListener.onPurchasesUpdated(cancelledResult, null)
            assertEquals(BillingEvent.PurchaseCancelled, awaitItem())
        }
    }

    @Test
    fun `purchases update with network failure emits NetworkUnavailable`() = runTest {
        val networkErrorResult = mockk<BillingResult> {
            every { responseCode } returns BillingClient.BillingResponseCode.NETWORK_ERROR
        }

        manager.events.test {
            capturedListener.onPurchasesUpdated(networkErrorResult, null)
            assertEquals(BillingEvent.NetworkUnavailable, awaitItem())
        }
    }

    @Test
    fun `restore purchases refreshes existing purchases from Play`() = runTest {
        stubSuccessfulConnection()
        manager.startConnection()

        val restoredPurchase = mockk<Purchase>(relaxed = true) {
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            every { isAcknowledged } returns true
        }
        every { billingClient.queryPurchasesAsync(any<QueryPurchasesParams>(), any<PurchasesResponseListener>()) } answers {
            secondArg<PurchasesResponseListener>().onQueryPurchasesResponse(okResult, listOf(restoredPurchase))
        }

        manager.refresh()

        assertEquals(listOf(restoredPurchase), manager.purchases.value)
    }

    @Test
    fun `refresh starts connection when client is not ready`() = runTest {
        every { billingClient.isReady } returns false
        stubSuccessfulConnection()

        manager.refresh()

        verify { billingClient.startConnection(any()) }
    }

    @Test
    fun `launchPurchaseFlow without network emits NetworkUnavailable`() = runTest {
        setNetworkAvailable(false)

        manager.events.test {
            manager.launchPurchaseFlow(mockk<Activity>(), BillingProducts.SUBSCRIPTION_YEARLY)
            assertEquals(BillingEvent.NetworkUnavailable, awaitItem())
        }
        verify(exactly = 0) { billingClient.launchBillingFlow(any(), any()) }
    }

    @Test
    fun `launchPurchaseFlow when client not ready emits error and reconnects`() = runTest {
        every { billingClient.isReady } returns false

        manager.events.test {
            manager.launchPurchaseFlow(mockk<Activity>(), BillingProducts.SUBSCRIPTION_YEARLY)
            assertEquals(
                BillingEvent.PurchaseError("Store connection isn't ready yet. Please try again."),
                awaitItem()
            )
        }
        verify { billingClient.startConnection(any()) }
    }

    @Test
    fun `launchPurchaseFlow for unknown product emits error`() = runTest {
        every { billingClient.isReady } returns true

        manager.events.test {
            manager.launchPurchaseFlow(mockk<Activity>(), "unknown.product")
            assertEquals(BillingEvent.PurchaseError("This product isn't available right now."), awaitItem())
        }
    }

    @Test
    fun `launchPurchaseFlow launches billing flow for available product`() = runTest {
        stubSuccessfulConnection(
            listOf(
                mockk<ProductDetails>(relaxed = true) {
                    every { productId } returns BillingProducts.SUBSCRIPTION_LIFETIME
                    every { productType } returns BillingClient.ProductType.INAPP
                }
            )
        )
        manager.startConnection()
        every { billingClient.launchBillingFlow(any(), any<BillingFlowParams>()) } returns okResult

        manager.launchPurchaseFlow(mockk<Activity>(), BillingProducts.SUBSCRIPTION_LIFETIME)

        verify { billingClient.launchBillingFlow(any(), any<BillingFlowParams>()) }
    }

    @Test
    fun `endConnection delegates to billing client`() {
        manager.endConnection()
        verify { billingClient.endConnection() }
    }
}
