package com.wesee.app.ui.screens.disabled

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.wesee.app.network.ApiResult
import com.wesee.app.network.AtlasApi
import com.wesee.app.network.AuthSession
import com.wesee.app.network.RegisterBody
import com.wesee.app.network.UserType
import com.wesee.app.network.spokenFailure
import com.wesee.app.voice.FieldKind
import com.wesee.app.voice.VoiceStep
import kotlinx.coroutines.launch

private val registerSteps = listOf(
    VoiceStep(FieldKind.Name, "Qual é o seu nome?"),
    VoiceStep(FieldKind.Email, "Agora diga o seu e-mail."),
    VoiceStep(FieldKind.Password, "Agora diga a senha que você deseja utilizar, com 6 números.", secret = true),
    VoiceStep(FieldKind.ConfirmPassword, "Para confirmar, diga novamente a sua senha.", secret = true),
)

/**
 * Tela 6 — Cadastro do usuário PCD visual (100% por voz) no backend (/auth/register) e login automático.
 * [onRegistered] recebe a sessão, ou null se o cadastro deu certo mas o login automático falhou.
 */
@Composable
fun DisabledRegisterScreen(
    onRegistered: (AuthSession?) -> Unit = {},
    canUseBiometric: Boolean = false,
    onBiometricLogin: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val host = rememberVoiceHost(registerSteps, "Cadastro por voz.") { session, answers ->
        scope.launch {
            val nome = answers[FieldKind.Name].orEmpty()
            val email = answers[FieldKind.Email].orEmpty()
            val senha = answers[FieldKind.Password].orEmpty()
            when (val r = AtlasApi.register(RegisterBody(nome, email, senha, tipo = UserType.PCD_VISUAL))) {
                is ApiResult.Success -> when (val l = AtlasApi.login(email, senha)) {
                    is ApiResult.Success ->
                        session.complete("Cadastro concluído. Bem-vindo, ${nome.substringBefore(' ')}.") { onRegistered(l.value) }
                    is ApiResult.Error ->
                        session.complete("Cadastro concluído. Agora faça o login.") { onRegistered(null) }
                }
                is ApiResult.Error -> when {
                    r.code == 409 -> session.retryFrom(FieldKind.Email, "Esse e-mail já está cadastrado. Diga outro e-mail.")
                    r.code == 400 -> {
                        val key = when {
                            r.fields.containsKey("senha") -> FieldKind.Password
                            r.fields.containsKey("email") -> FieldKind.Email
                            else -> FieldKind.Name
                        }
                        session.retryFrom(key, r.fields.values.firstOrNull() ?: "Dados inválidos.")
                    }
                    else -> session.failSubmit(r.spokenFailure())
                }
            }
        }
    }
    VoiceAuthScreen("Cadastro", host) {
        if (canUseBiometric) { host.session.announce("Login concluído."); onBiometricLogin() }
        else host.session.announce("Para usar a biometria, entre uma vez com e-mail e senha.")
    }
}
