package com.wesee.app.ui.screens.guardian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
import com.wesee.app.network.RegisterBody
import com.wesee.app.network.UserType
import com.wesee.app.ui.components.*
import com.wesee.app.ui.theme.WeSeeCard
import com.wesee.app.ui.theme.WeSeeSoftText
import kotlinx.coroutines.launch

/** Tela 4 — Cadastro do responsável (/auth/register). Regras do backend: senha de 6 números, CPF 11, telefone 10–11 dígitos. */
@Composable
fun GuardianRegisterScreen(
    onRegistered: () -> Unit = {},
    onGoToLogin: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var cpf by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun submit() {
        if (loading) return
        val cpfDigits = cpf.filter { it.isDigit() }
        val phoneDigits = phone.filter { it.isDigit() }
        error = when {
            name.isBlank() -> "Informe seu nome."
            !email.contains("@") -> "Informe um e-mail válido."
            !Regex("\\d{6}").matches(password) -> "A senha deve ter exatamente 6 números."
            password != confirm -> "As senhas não são iguais."
            cpfDigits.isNotEmpty() && cpfDigits.length != 11 -> "O CPF deve ter 11 números."
            phoneDigits.isNotEmpty() && phoneDigits.length !in 10..11 -> "O telefone deve ter DDD + número (10 ou 11 dígitos)."
            else -> null
        }
        if (error != null) return
        scope.launch {
            loading = true
            val body = RegisterBody(name.trim(), email.trim(), password, cpfDigits.ifEmpty { null }, phoneDigits.ifEmpty { null }, UserType.RESPONSAVEL)
            when (val r = AtlasApi.register(body)) {
                is ApiResult.Success -> onRegistered()
                is ApiResult.Error -> error = when (r.code) {
                    409 -> "Esse e-mail ou CPF já está cadastrado."
                    -1 -> "Sem conexão com o servidor."
                    else -> r.fields.values.firstOrNull() ?: r.message
                }
            }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().background(WeSeeCard).imePadding()) {
        Column(Modifier.weight(1f).fillMaxWidth().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            WeSeeHeader(2, topPadding = 24.dp)
            Spacer(Modifier.height(24.dp))
            WeSeeEyes(R.drawable.wesee_eyes_guardian)
        }
        WeSeeBottomPanel {
            WeSeeText(stringResource(R.string.guardian_register_title), Color.White, 40, FontWeight.SemiBold, textAlign = TextAlign.Start)
            WeSeeText(stringResource(R.string.guardian_register_subtitle), WeSeeSoftText, 12, textAlign = TextAlign.Start)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WeSeeTextField(R.drawable.ic_id_card, stringResource(R.string.field_name_hint), name, { name = it })
                WeSeeTextField(R.drawable.ic_mail, stringResource(R.string.field_email_hint), email, { email = it },
                    keyboardType = KeyboardType.Email)
                WeSeeTextField(R.drawable.ic_lock, stringResource(R.string.field_password_hint), password, { password = it },
                    isPassword = true)
                WeSeeTextField(R.drawable.ic_lock, stringResource(R.string.field_confirm_password_hint), confirm, { confirm = it },
                    isPassword = true)
                WeSeeTextField(R.drawable.ic_id_card, stringResource(R.string.field_cpf_hint), cpf, { cpf = it },
                    keyboardType = KeyboardType.Number)
                WeSeeTextField(R.drawable.ic_phone, stringResource(R.string.field_phone_hint), phone, { phone = it },
                    keyboardType = KeyboardType.Phone)
            }
            error?.let {
                Spacer(Modifier.height(8.dp))
                WeSeeText(it, ErrorColor, 11, FontWeight.SemiBold, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
            Spacer(Modifier.height(12.dp))
            PanelButton(if (loading) "Aguarde..." else stringResource(R.string.action_register), onClick = ::submit)
            Spacer(Modifier.height(10.dp))
            AuthFooter(stringResource(R.string.has_account_prefix), stringResource(R.string.action_login_link), onGoToLogin)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable private fun GuardianRegisterPreview() = GuardianRegisterScreen()
