package com.wesee.app.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

private tailrec fun Context.findActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Autentica com a biometria do aparelho, se disponível. */
fun authenticateWithBiometrics(context: Context, onSuccess: () -> Unit, onUnavailable: (String) -> Unit) {
    val activity = context.findActivity() ?: return onUnavailable("Biometria indisponível.")
    val allowed = BIOMETRIC_STRONG or BIOMETRIC_WEAK
    if (BiometricManager.from(activity).canAuthenticate(allowed) != BiometricManager.BIOMETRIC_SUCCESS) {
        return onUnavailable("Biometria indisponível ou não cadastrada neste aparelho.")
    }
    val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
        })
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login com biometria")
            .setAllowedAuthenticators(allowed)
            .setNegativeButtonText("Cancelar")
            .build(),
    )
}
