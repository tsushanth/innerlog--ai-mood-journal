package com.factory.innerlogaimoodjournal.ui.screens.settings

import app.cash.turbine.test
import com.factory.innerlogaimoodjournal.billing.BillingManager
import com.factory.innerlogaimoodjournal.billing.PremiumManager
import com.factory.innerlogaimoodjournal.security.LockManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {

    private lateinit var lockManager: LockManager
    private lateinit var billingManager: BillingManager
    private lateinit var premiumManager: PremiumManager
    private val isPremiumFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        lockManager = mockk(relaxed = true)
        billingManager = mockk(relaxed = true)
        premiumManager = mockk()
        every { premiumManager.isPremium } returns isPremiumFlow
    }

    private fun viewModel() = SettingsViewModel(lockManager, billingManager, premiumManager)

    @Test
    fun `initial state mirrors lock manager`() {
        every { lockManager.isLockEnabled() } returns true
        every { lockManager.isBiometricEnabled() } returns true

        val vm = viewModel()

        assertTrue(vm.lockEnabled)
        assertTrue(vm.biometricEnabled)
    }

    @Test
    fun `disableLock clears pin and resets state`() {
        every { lockManager.isLockEnabled() } returns true
        every { lockManager.isBiometricEnabled() } returns true
        val vm = viewModel()

        vm.disableLock()

        verify { lockManager.clearPin() }
        assertFalse(vm.lockEnabled)
        assertFalse(vm.biometricEnabled)
    }

    @Test
    fun `setBiometricEnabled updates lock manager and local state`() {
        every { lockManager.isLockEnabled() } returns false
        every { lockManager.isBiometricEnabled() } returns false
        val vm = viewModel()

        vm.setBiometricEnabled(true)

        verify { lockManager.setBiometricEnabled(true) }
        assertTrue(vm.biometricEnabled)
    }

    @Test
    fun `refresh re-reads lock state from lock manager`() {
        every { lockManager.isLockEnabled() } returns false
        every { lockManager.isBiometricEnabled() } returns false
        val vm = viewModel()

        every { lockManager.isLockEnabled() } returns true
        every { lockManager.isBiometricEnabled() } returns true
        vm.refresh()

        assertTrue(vm.lockEnabled)
        assertTrue(vm.biometricEnabled)
    }

    @Test
    fun `restorePurchases delegates to billing manager refresh`() {
        every { lockManager.isLockEnabled() } returns false
        every { lockManager.isBiometricEnabled() } returns false
        val vm = viewModel()

        vm.restorePurchases()

        verify { billingManager.refresh() }
    }

    @Test
    fun `isPremium reflects premium manager flow`() = runTest {
        every { lockManager.isLockEnabled() } returns false
        every { lockManager.isBiometricEnabled() } returns false
        val vm = viewModel()

        vm.isPremium.test {
            assertFalse(awaitItem())
            isPremiumFlow.value = true
            assertTrue(awaitItem())
        }
    }
}
