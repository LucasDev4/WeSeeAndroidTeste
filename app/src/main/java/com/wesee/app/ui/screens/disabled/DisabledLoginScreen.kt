package com.wesee.app.ui.screens.disabled

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.wesee.app.network.ApiResult
import com.wesee.app.network.AtlasApi
import com.wesee.app.network.AuthSession
import com.wesee.app.network.UserType
import com.wesee.app.network.spokenFailure
import com.wesee.app.voice.FieldKind
import com.wesee.app.voice.VoiceStep
import kotlinx.coroutines.launch

private val loginSteps = listOf(
    VoiceStep(FieldKind.Email, "Qual é o seu e-mail?"),
    VoiceStep(FieldKind.Password, "Agora diga sua senha de 6 números.", secret = true),
)

/** Tela 5 — Login do usuário PCD visual (100% por voz) autenticando no backend (/auth/login). */
@Composable
fun DisabledLoginScreen(
    onLoggedIn: (AuthSession) -> Unit = {},
    canUseBiometric: Boolean = false,
    onBiometricLogin: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val host = rememberVoiceHost(loginSteps, "Login por voz.") { session, answers ->
        scope.launch {
            val email = answers[FieldKind.Email].orEmpty()
            when (val r = AtlasApi.login(email, answers[FieldKind.Password].orEmpty())) {
                is ApiResult.Success ->
                    if (r.value.tipo == UserType.PCD_VISUAL)
                        session.complete("Login concluído. Olá, ${r.value.nome.substringBefore(' ')}.") { onLoggedIn(r.value) }
                    else session.retryFrom(FieldKind.Email, "Essa conta é de responsável. Use uma conta de usuário.")
                is ApiResult.Error -> when {
                    r.code == 401 || r.code == 404 -> session.retryFrom(FieldKind.Email, "E-mail ou senha incorretos.")
                    r.code == 400 -> session.retryFrom(FieldKind.Email, r.fields.values.firstOrNull() ?: "Dados inválidos.")
                    else -> session.failSubmit(r.spokenFailure())
                }
            }
        }
    }
    VoiceAuthScreen("Login", host) {
        if (canUseBiometric) { host.session.announce("Login concluído."); onBiometricLogin() }
        else host.session.announce("Para usar a biometria, entre uma vez com e-mail e senha.")
    }
}
