package com.wesee.app.ui.screens.guardian.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesee.app.data.Notificacao
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeDeep

/** Painel lateral (entra pela direita) sobre a tela, com camada escurecida. Fica acima da barra de navegação. */
@Composable
fun NotificationsPanelOverlay(visible: Boolean, items: List<Notificacao>, onDismiss: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val panelWidth = (maxWidth * 0.4f).coerceAtLeast(150.dp)
        AnimatedVisibility(visible, enter = fadeIn(tween(220)), exit = fadeOut(tween(200))) {
            Box(
                Modifier.fillMaxSize().background(Color(0x8C5B6B7D))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                    .semantics { contentDescription = "Fechar notificações" },
            )
        }
        AnimatedVisibility(
            visible, modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(tween(260)) { it }, exit = slideOutHorizontally(tween(220)) { it },
        ) {
            Column(
                Modifier.width(panelWidth).fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)).background(Color(0xFFE6EFF9))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                    .statusBarsPadding().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 24.dp),
            ) {
                WeSeeText("Notificações", WeSeeBlue, 18, FontWeight.Bold, Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(WeSeeBlue))
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items.forEach { n ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(Modifier.padding(top = 4.dp).size(6.dp).clip(CircleShape).background(AlertRed))
                            Spacer(Modifier.width(6.dp))
                            WeSeeText(n.mensagem, WeSeeDeep, 10, FontWeight.SemiBold, Modifier.weight(1f), textAlign = TextAlign.Start)
                        }
                    }
                }
            }
        }
    }
}
