package com.wesee.app.ui.screens.pcd.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wesee.app.R
import com.wesee.app.ui.components.PcdBottomBar
import com.wesee.app.ui.components.PcdTab
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.IstokWeb
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard
import com.wesee.app.ui.theme.WeSeeMid

/** Sugestões estáticas. FUTURO: virão da IA do Navi. */
private val suggestions = listOf(
    "Onde estou?", "Como está o tempo?", "Me ajude com uma tarefa",
    "O que tenho para hoje?", "Ler minhas anotações", "Me dê uma orientação",
)

@Composable
private fun Glow() {
    Canvas(Modifier.fillMaxSize()) {
        val a = Offset(size.width, size.height * 0.03f)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF5E9BD6).copy(alpha = 0.75f), Color.Transparent), a, size.width * 0.45f),
            radius = size.width * 0.45f, center = a)
        val b = Offset(0f, size.height * 0.62f)
        drawCircle(Brush.radialGradient(listOf(Color(0xFF6FA8E8).copy(alpha = 0.40f), Color.Transparent), b, size.width * 0.35f),
            radius = size.width * 0.35f, center = b)
    }
}

/** Botão de voz do Navi (visual por enquanto). FUTURO: ativa o microfone e conversa com a IA. */
@Composable
private fun VoiceButton() {
    Box(
        Modifier.size(176.dp).shadow(14.dp, CircleShape, ambientColor = WeSeeBlue.copy(alpha = 0.3f), spotColor = WeSeeBlue.copy(alpha = 0.3f))
            .clip(CircleShape).background(Brush.verticalGradient(listOf(Color(0xFFEAF3FD), Color(0xFF8DBFFF)))),
        contentAlignment = Alignment.Center,
    ) { Image(painterResource(R.drawable.ic_mic_blue), "Microfone do Navi, disponível em breve", Modifier.size(34.dp)) }
}

/** Barra de mensagem (visual por enquanto). FUTURO: Usuário → mensagem → Navi → resposta da IA. */
@Composable
private fun MessageBar() {
    val pill = RoundedCornerShape(50)
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth().shadow(8.dp, pill).clip(pill)
            .background(Color.White).padding(start = 18.dp, end = 7.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeSeeText("Pergunte para Navi algum lugar novo...", Color(0xFF9AA7B4), 12, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        Box(Modifier.size(36.dp).clip(CircleShape).background(WeSeeBlue), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_arrow_up), "Enviar, disponível em breve", Modifier.size(20.dp))
        }
    }
}

/** Chat do usuário PCD visual — somente interface. */
@Composable
fun PcdChatScreen(onOpenHome: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(WeSeeCard)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Glow()
            Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 18.dp)) {
                Image(painterResource(R.drawable.wesee_eyes_chat), null, Modifier.width(112.dp))
                WeSeeText("Navi", WeSeeBlue, 52, FontWeight.Bold, textAlign = TextAlign.Start, fontFamily = IstokWeb, letterSpacing = 6.sp)
                WeSeeText("Seu parceiro pessoal", WeSeeMid, 18, textAlign = TextAlign.Start, fontFamily = IstokWeb)
                Spacer(Modifier.height(28.dp))
                Box(Modifier.align(Alignment.CenterHorizontally)) { VoiceButton() }
                Spacer(Modifier.height(24.dp))
                WeSeeText("Sugestões", WeSeeBlue, 20, modifier = Modifier.fillMaxWidth(), fontFamily = IstokWeb)
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { text ->
                                Box(Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(12.dp)).background(WeSeeBlue).padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center) {
                                    WeSeeText(text, Color.White, 11, FontWeight.SemiBold, lineHeight = 13)
                                }
                            }
                        }
                    }
                }
            }
        }
        MessageBar()
        PcdBottomBar(PcdTab.Chat, onHome = onOpenHome, onChat = {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable private fun ChatPreview() = PcdChatScreen()
