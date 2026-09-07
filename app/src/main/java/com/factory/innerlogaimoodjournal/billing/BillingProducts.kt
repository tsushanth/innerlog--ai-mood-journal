package com.factory.innerlogaimoodjournal.billing

enum class ProductKind { SUBSCRIPTION, ONE_TIME }

data class PremiumProduct(
    val productId: String,
    val kind: ProductKind,
    val title: String,
    val description: String,
    val fallbackPrice: String,
    val fallbackBillingPeriod: String? = null
)

object BillingProducts {
    private const val APP_ID = "com.factory.innerlogaimoodjournal"

    const val SUBSCRIPTION_WEEKLY = "$APP_ID.subscription.weekly"
    const val SUBSCRIPTION_MONTHLY = "$APP_ID.subscription.monthly"
    const val SUBSCRIPTION_YEARLY = "$APP_ID.subscription.yearly"
    const val SUBSCRIPTION_LIFETIME = "$APP_ID.subscription.lifetime"
    const val IAP_SMALL = "$APP_ID.small_iap"

    val SUBSCRIPTION_PRODUCT_IDS = listOf(SUBSCRIPTION_WEEKLY, SUBSCRIPTION_MONTHLY, SUBSCRIPTION_YEARLY)
    val ONE_TIME_PRODUCT_IDS = listOf(SUBSCRIPTION_LIFETIME, IAP_SMALL)

    val PREMIUM_ENTITLEMENT_IDS = setOf(
        SUBSCRIPTION_WEEKLY,
        SUBSCRIPTION_MONTHLY,
        SUBSCRIPTION_YEARLY,
        SUBSCRIPTION_LIFETIME
    )

    val PAYWALL_PRODUCTS = listOf(
        PremiumProduct(
            productId = SUBSCRIPTION_YEARLY,
            kind = ProductKind.SUBSCRIPTION,
            title = "Yearly",
            description = "Best value — full access, billed annually",
            fallbackPrice = "$14.39",
            fallbackBillingPeriod = "/year"
        ),
        PremiumProduct(
            productId = SUBSCRIPTION_LIFETIME,
            kind = ProductKind.ONE_TIME,
            title = "Lifetime",
            description = "Pay once, unlock InnerLog Premium forever",
            fallbackPrice = "$29.99"
        ),
        PremiumProduct(
            productId = IAP_SMALL,
            kind = ProductKind.ONE_TIME,
            title = "Support InnerLog",
            description = "A small one-time tip to support development",
            fallbackPrice = "$0.99"
        )
    )
}
