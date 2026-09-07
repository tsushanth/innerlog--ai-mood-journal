package com.factory.innerlogaimoodjournal.ui.screens.lock

import com.factory.innerlogaimoodjournal.security.LockManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LockViewModelTest {

    private lateinit var lockManager: LockManager
    private lateinit var vm: LockViewModel

    @Before
    fun setUp() {
        lockManager = mockk()
        vm = LockViewModel(lockManager)
    }

    @Test
    fun `initial state has empty pin and no error`() {
        assertEquals("", vm.pinInput)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `onDigitEntered appends digits up to four`() {
        vm.onDigitEntered("1")
        vm.onDigitEntered("2")
        vm.onDigitEntered("3")
        vm.onDigitEntered("4")
        vm.onDigitEntered("5")

        assertEquals("1234", vm.pinInput)
    }

    @Test
    fun `onDigitEntered clears a previous error`() {
        every { lockManager.verifyPin(any()) } returns false
        vm.onDigitEntered("1")
        vm.onDigitEntered("1")
        vm.onDigitEntered("1")
        vm.onDigitEntered("1")
        vm.verify {}
        assertEquals("Incorrect PIN", vm.errorMessage)

        vm.onDigitEntered("9")

        assertNull(vm.errorMessage)
    }

    @Test
    fun `onBackspace removes the last digit`() {
        vm.onDigitEntered("1")
        vm.onDigitEntered("2")

        vm.onBackspace()

        assertEquals("1", vm.pinInput)
    }

    @Test
    fun `onBackspace on empty pin is a no-op`() {
        vm.onBackspace()
        assertEquals("", vm.pinInput)
    }

    @Test
    fun `isBiometricEnabled delegates to lock manager`() {
        every { lockManager.isBiometricEnabled() } returns true
        assertTrue(vm.isBiometricEnabled())
    }

    @Test
    fun `onBiometricError sets the error message`() {
        vm.onBiometricError("Sensor error")
        assertEquals("Sensor error", vm.errorMessage)
    }

    @Test
    fun `verify with correct pin clears input and invokes success callback`() {
        every { lockManager.verifyPin("1234") } returns true
        vm.onDigitEntered("1")
        vm.onDigitEntered("2")
        vm.onDigitEntered("3")
        vm.onDigitEntered("4")
        var succeeded = false

        vm.verify { succeeded = true }

        assertTrue(succeeded)
        assertEquals("", vm.pinInput)
        assertNull(vm.errorMessage)
    }

    @Test
    fun `verify with incorrect pin shows error and clears input without success callback`() {
        every { lockManager.verifyPin("0000") } returns false
        vm.onDigitEntered("0")
        vm.onDigitEntered("0")
        vm.onDigitEntered("0")
        vm.onDigitEntered("0")
        var succeeded = false

        vm.verify { succeeded = true }

        assertFalse(succeeded)
        assertEquals("Incorrect PIN", vm.errorMessage)
        assertEquals("", vm.pinInput)
        verify { lockManager.verifyPin("0000") }
    }
}
