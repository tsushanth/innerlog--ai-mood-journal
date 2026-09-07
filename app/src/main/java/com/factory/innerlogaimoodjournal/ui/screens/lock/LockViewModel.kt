package com.factory.innerlogaimoodjournal.ui.screens.lock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.factory.innerlogaimoodjournal.security.LockManager

class LockViewModel(private val lockManager: LockManager) : ViewModel() {

    var pinInput by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onDigitEntered(digit: String) {
        if (pinInput.length < 4) {
            pinInput += digit
            errorMessage = null
        }
    }

    fun onBackspace() {
        if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
    }

    fun isBiometricEnabled(): Boolean = lockManager.isBiometricEnabled()

    fun onBiometricError(message: String) {
        errorMessage = message
    }

    fun verify(onSuccess: () -> Unit) {
        if (lockManager.verifyPin(pinInput)) {
            pinInput = ""
            onSuccess()
        } else {
            errorMessage = "Incorrect PIN"
            pinInput = ""
        }
    }
}
