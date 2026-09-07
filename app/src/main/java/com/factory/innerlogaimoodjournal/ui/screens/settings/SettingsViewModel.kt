package com.factory.innerlogaimoodjournal.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.factory.innerlogaimoodjournal.billing.BillingManager
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.security.LockManager
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val lockManager: LockManager,
    private val billingManager: BillingManager,
    premiumManager: PremiumManager
) : ViewModel() {

    var lockEnabled by mutableStateOf(lockManager.isLockEnabled())
        private set
    var biometricEnabled by mutableStateOf(lockManager.isBiometricEnabled())
        private set

    val isPremium: StateFlow<Boolean> = premiumManager.isPremium

    fun disableLock() {
        lockManager.clearPin()
        lockEnabled = false
        biometricEnabled = false
    }

    fun setBiometricEnabled(enabled: Boolean) {
        lockManager.setBiometricEnabled(enabled)
        biometricEnabled = enabled
    }

    fun refresh() {
        lockEnabled = lockManager.isLockEnabled()
        biometricEnabled = lockManager.isBiometricEnabled()
    }

    fun restorePurchases() {
        billingManager.refresh()
    }
}
