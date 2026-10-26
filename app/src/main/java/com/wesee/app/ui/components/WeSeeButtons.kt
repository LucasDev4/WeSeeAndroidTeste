package com.wesee.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard

/** Botões grandes da tela inicial (Login = secundário, Cadastro = principal). */
@Composable
fun AppButton(label: String, primary: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.fillMaxWidth(0.88f).height(44.dp)
            .then(if (primary) Modifier else Modifier.shadow(6.dp, shape))
            .clip(shape).background(if (primary) WeSeeBlue else WeSeeCard)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { WeSeeText(label, if (primary) Color.White else WeSeeBlue, 16, FontWeight.Bold) }
}

/** Botão claro usado dentro dos painéis azuis. */
@Composable
fun PanelButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.fillMaxWidth().height(40.dp).clip(shape).background(WeSeeCard)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { WeSeeText(label, WeSeeBlue, 16, FontWeight.Bold) }
}

/** "Não tem conta? Criar conta" — texto auxiliar com ação clicável. */
@Composable
fun AuthFooter(prefix: String, action: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier) {
        WeSeeText(prefix, com.wesee.app.ui.theme.WeSeeSoftText, 11, textAlign = androidx.compose.ui.text.style.TextAlign.Start)
        WeSeeText(action, Color.White, 11, FontWeight.SemiBold,
            Modifier.clickable(role = Role.Button, onClick = onClick),
            textAlign = androidx.compose.ui.text.style.TextAlign.Start)
    }
}
