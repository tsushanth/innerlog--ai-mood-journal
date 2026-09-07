package com.factory.innerlogaimoodjournal.ui.screens.paywall

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.billingclient.api.ProductDetails
import com.factory.innerlogaimoodjournal.billing.BillingEvent
import com.factory.innerlogaimoodjournal.billing.BillingProducts
import com.factory.innerlogaimoodjournal.billing.PremiumProduct
import com.factory.innerlogaimoodjournal.billing.ProductKind
import com.factory.innerlogaimoodjournal.ui.currentApp

private const val TERMS_OF_SERVICE_URL = "https://www.innerlogapp.com/terms"
private const val PRIVACY_POLICY_URL = "https://www.innerlogapp.com/privacy"

private val premiumHighlights = listOf(
    "Unlimited AI mood insights on every journal entry",
    "Full habit tracking with streaks and reminders",
    "Goal tracking with progress trends",
    "Weekly mood analytics and insights",
    "Biometric unlock for extra privacy"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(activity: Activity, onClose: () -> Unit) {
    val app = currentApp()
    val viewModel: PaywallViewModel = viewModel(factory = viewModelFactory {
        initializer { PaywallViewModel(app.billingManager, app.premiumManager) }
    })

    val isPremium by viewModel.isPremium.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val productDetails by viewModel.productDetails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(isPremium) {
        if (isPremium) onClose()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                BillingEvent.PurchaseSuccess -> "Welcome to InnerLog Premium!"
                BillingEvent.PurchaseCancelled -> "Purchase cancelled."
                BillingEvent.PurchasePending -> "Your purchase is pending. Premium will unlock once it completes."
                BillingEvent.NetworkUnavailable -> "No internet connection. Please check your connection and try again."
                is BillingEvent.PurchaseError -> event.message
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InnerLog Premium") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(32.dp).width(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Unlock the full InnerLog experience",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(Modifier.height(20.dp))

            premiumHighlights.forEach { highlight ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(highlight, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (!isConnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.WifiOff, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Connecting to Google Play... check your connection if this doesn't resolve.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            BillingProducts.PAYWALL_PRODUCTS.forEach { product ->
                PricingCard(
                    product = product,
                    details = productDetails[product.productId],
                    onClick = { viewModel.purchase(activity, product.productId) }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { openUrl(context, TERMS_OF_SERVICE_URL) }) {
                    Text("Terms of Service", style = MaterialTheme.typography.labelLarge)
                }
                TextButton(onClick = { openUrl(context, PRIVACY_POLICY_URL) }) {
                    Text("Privacy Policy", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun PricingCard(
    product: PremiumProduct,
    details: ProductDetails?,
    onClick: () -> Unit
) {
    val priceText = formattedPrice(product, details)
    val haptics = LocalHapticFeedback.current
    val handleClick = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(product.description, style = MaterialTheme.typography.labelLarge)
                }
                Text(priceText, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            if (product.kind == ProductKind.SUBSCRIPTION) {
                Button(
                    onClick = handleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Subscribe to ${product.title}, $priceText" }
                ) {
                    Text("Subscribe")
                }
            } else {
                OutlinedButton(
                    onClick = handleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Purchase ${product.title}, $priceText" }
                ) {
                    Text("Purchase")
                }
            }
        }
    }
}

private fun formattedPrice(product: PremiumProduct, details: ProductDetails?): String {
    if (details == null) {
        return product.fallbackPrice + (product.fallbackBillingPeriod ?: "")
    }
    return when (product.kind) {
        ProductKind.SUBSCRIPTION -> {
            val offer = details.subscriptionOfferDetails?.firstOrNull()
            val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
            phase?.formattedPrice?.let { "$it/${billingPeriodLabel(phase.billingPeriod)}" }
                ?: (product.fallbackPrice + (product.fallbackBillingPeriod ?: ""))
        }
        ProductKind.ONE_TIME -> {
            details.oneTimePurchaseOfferDetails?.formattedPrice ?: product.fallbackPrice
        }
    }
}

private fun billingPeriodLabel(isoPeriod: String?): String = when (isoPeriod) {
    "P1W" -> "week"
    "P1M" -> "month"
    "P1Y" -> "year"
    else -> "period"
}

private fun openUrl(context: android.content.Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // No browser available on this device; nothing to fall back to.
    }
}
