package com.factory.innerlogaimoodjournal.billing

sealed class BillingEvent {
    data object PurchaseSuccess : BillingEvent()
    data object PurchaseCancelled : BillingEvent()
    data object PurchasePending : BillingEvent()
    data object NetworkUnavailable : BillingEvent()
    data class PurchaseError(val message: String) : BillingEvent()
}
