package com.wesee.app.ui.screens.guardian

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.network.ApiResult
import com.wesee.app.network.AtlasApi
import com.wesee.app.network.AuthSession
import com.wesee.app.network.UserType
import com.wesee.app.ui.components.*
import com.wesee.app.ui.theme.WeSeeCard
import com.wesee.app.ui.theme.WeSeeSoftText
import kotlinx.coroutines.launch

internal val ErrorColor = Color(0xFFFFC9C9)
internal val OkColor = Color(0xFFBFF0CE)

/** Tela 3 — Login do responsável (/auth/login). A área do responsável será feita depois. */
@Composable
fun GuardianLoginScreen(
    onLoggedIn: (AuthSession) -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var message by remember { mutableStateOf<Pair<String, Boolean>?>(null) } // texto, éSucesso
    var loading by remember { mutableStateOf(false) }

    fun submit() {
        if (loading) return
        if (!email.contains("@")) { message = "Informe um e-mail válido." to false; return }
        if (password.isBlank()) { message = "Informe sua senha." to false; return }
        scope.launch {
            loading = true; message = null
            when (val r = AtlasApi.login(email.trim(), password)) {
                is ApiResult.Success ->
                    if (r.value.tipo == UserType.RESPONSAVEL) {
                        onLoggedIn(r.value)
                    } else message = "Essa conta é de usuário PCD. Entre pelo modo PCD visual." to false
                is ApiResult.Error -> {
                    val text = when (r.code) {
                        401, 404 -> "E-mail ou senha incorretos."
                        -1 -> "Sem conexão com o servidor."
                        else -> r.fields.values.firstOrNull() ?: r.message
                    }
                    message = text to false
                }
            }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(WeSeeCard).imePadding()) {
        Column(Modifier.weight(1f).fillMaxWidth().statusBarsPadding(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            WeSeeHeader(2)
            Spacer(Modifier.height(40.dp))
            WeSeeEyes(R.drawable.wesee_eyes_guardian)
        }
        WeSeeBottomPanel(Modifier.heightIn(min = 330.dp)) {
            WeSeeText(stringResource(R.string.guardian_login_title), Color.White, 40, FontWeight.SemiBold, textAlign = TextAlign.Start)
            WeSeeText(stringResource(R.string.guardian_login_subtitle), WeSeeSoftText, 12, textAlign = TextAlign.Start)
            Spacer(Modifier.height(16.dp))
            WeSeeTextField(R.drawable.ic_mail, stringResource(R.string.field_email_hint), email, { email = it },
                keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            WeSeeTextField(R.drawable.ic_lock, stringResource(R.string.field_password_hint), password, { password = it },
                isPassword = true)
            Spacer(Modifier.height(6.dp))
            WeSeeText(stringResource(R.string.forgot_password), WeSeeSoftText, 11,
                modifier = Modifier.clickable(onClick = onForgotPassword), textAlign = TextAlign.Start)
            message?.let { (text, ok) ->
                Spacer(Modifier.height(8.dp))
                WeSeeText(text, if (ok) OkColor else ErrorColor, 11, FontWeight.SemiBold, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            Spacer(Modifier.height(16.dp))
            PanelButton(if (loading) "Aguarde..." else stringResource(R.string.action_login), onClick = ::submit)
            Spacer(Modifier.height(10.dp))
            AuthFooter(stringResource(R.string.no_account_prefix), stringResource(R.string.create_account), onCreateAccount)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable private fun GuardianLoginPreview() = GuardianLoginScreen()
