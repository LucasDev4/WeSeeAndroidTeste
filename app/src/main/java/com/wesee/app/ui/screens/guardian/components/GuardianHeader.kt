package com.wesee.app.ui.screens.guardian.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.WeSeeBlue

val AlertRed = Color(0xFFF44343)
val StatusGreen = Color(0xFF00C853)

/** Cabeçalho do Responsável: avatar, saudação, sino com indicador de não lidas. Usado na Home e na tela de Localização. */
@Composable
fun GuardianHeader(greetingName: String, pessoaNome: String, hasUnread: Boolean, onBellClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(WeSeeBlue).semantics { contentDescription = "Avatar do responsável" })
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            WeSeeText("Olá $greetingName.", WeSeeBlue, 22, FontWeight.Bold, textAlign = TextAlign.Start)
            WeSeeText("Responsável de $pessoaNome", Color(0xFF6B7A8C), 9, textAlign = TextAlign.Start)
        }
        Box(
            Modifier.size(48.dp).clickable(role = Role.Button, onClickLabel = "Abrir notificações", onClick = onBellClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0xFFBFD0E3)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.ic_bell), if (hasUnread) "Notificações, há novas" else "Notificações", Modifier.size(15.dp))
            }
            if (hasUnread) Box(Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(8.dp).clip(CircleShape).background(AlertRed))
        }
    }
}

/** Indicador de dispositivo conectado (estático por enquanto). */
@Composable
fun DeviceConnectedIndicator(modifier: Modifier = Modifier) {
    Row(modifier.padding(start = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(StatusGreen))
        Spacer(Modifier.width(5.dp))
        WeSeeText("Dispositivo conectado", WeSeeBlue, 8, FontWeight.Bold)
    }
}
