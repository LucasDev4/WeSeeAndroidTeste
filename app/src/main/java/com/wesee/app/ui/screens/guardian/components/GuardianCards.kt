package com.wesee.app.ui.screens.guardian.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.data.Alerta
import com.wesee.app.data.Dispositivo
import com.wesee.app.data.PessoaAcompanhada
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.WeSeeBlue

private val Navy = Color(0xFF00172B)
private val CardShape = RoundedCornerShape(26.dp)
private val SoftBlue = Color(0xFF8FC9FF)

/** Card azul-marinho com localização resumida. Reutilizado na Home e na tela de Localização. */
@Composable
fun LocationSummaryCard(pessoa: PessoaAcompanhada, modifier: Modifier = Modifier) {
    Box(
        modifier.padding(horizontal = 18.dp).fillMaxWidth().heightIn(min = 128.dp).shadow(10.dp, CardShape).clip(CardShape)
            .background(Brush.verticalGradient(listOf(Navy, WeSeeBlue))).padding(16.dp),
    ) {
        WeSeeText("Localização de ${pessoa.nome}:", Color.White.copy(alpha = 0.8f), 9, textAlign = TextAlign.Start)
        Column(Modifier.align(Alignment.Center).fillMaxWidth().padding(top = 10.dp)) {
            WeSeeText(pessoa.endereco, Color.White, 19, FontWeight.SemiBold, Modifier.fillMaxWidth(), lineHeight = 25)
            WeSeeText(pessoa.ultimaAtualizacao, Color.White, 19, FontWeight.SemiBold, Modifier.fillMaxWidth(), lineHeight = 25)
        }
    }
}

@Composable
fun AlertsTitle(modifier: Modifier = Modifier) {
    Row(modifier.padding(start = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(R.drawable.ic_siren), "Alertas", Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        WeSeeText("Alertas Recentes", WeSeeBlue, 14, FontWeight.SemiBold)
    }
}

@Composable
private fun AlertCard(alerta: Alerta, selected: Boolean, modifier: Modifier) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier.height(78.dp)
            .then(if (selected) Modifier else Modifier.shadow(5.dp, shape))
            .clip(shape).background(if (selected) WeSeeBlue else Color(0xFFEFF5FB))
            .semantics(mergeDescendants = true) { contentDescription = "${alerta.titulo} ${alerta.horario}"; this.selected = selected }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        WeSeeText("${alerta.titulo}\n${alerta.horario}", if (selected) Color.White else SoftBlue, 10, FontWeight.SemiBold, lineHeight = 14)
    }
}

/** Carrossel horizontal de alertas (cartões de largura fixa, com "snap") + barra de posição. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlertsCarousel(alertas: List<Alerta>, modifier: Modifier = Modifier) {
    val state = rememberLazyListState()
    val density = LocalDensity.current
    val itemWidth = 132.dp
    val gap = 12.dp
    val pad = 18.dp
    val stepPx = with(density) { (itemWidth + gap).toPx() }
    val contentPx = with(density) { alertas.size * itemWidth.toPx() + (alertas.size - 1) * gap.toPx() + 2 * pad.toPx() }
    val scrolled by remember { derivedStateOf { state.firstVisibleItemIndex * stepPx + state.firstVisibleItemScrollOffset } }
    val selectedIndex = (scrolled / stepPx + 0.5f).toInt().coerceIn(0, alertas.lastIndex)
    val viewportPx = state.layoutInfo.viewportSize.width.toFloat()
    // parte preenchida = trecho do conteúdo já alcançado pela borda direita da tela
    val progress = if (viewportPx <= 0f || !state.canScrollForward) 1f else ((scrolled + viewportPx) / contentPx).coerceIn(0.1f, 1f)

    Column(modifier) {
        LazyRow(
            state = state, flingBehavior = rememberSnapFlingBehavior(state),
            contentPadding = PaddingValues(horizontal = pad, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            itemsIndexed(alertas, key = { _, a -> a.id }) { i, a -> AlertCard(a, i == selectedIndex, Modifier.width(itemWidth)) }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xFFEAF2FB))
            .semantics { contentDescription = "Posição no carrossel de alertas" }) {
            Box(Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(WeSeeBlue))
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String, positive: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        WeSeeText(label, Color.White, 10, FontWeight.Bold, Modifier.weight(1f), textAlign = TextAlign.Start)
        WeSeeText(value, Color.White, 10, FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (positive) StatusGreen else Color(0xFF8FA3B8)))
    }
}

/** Card "Status de …" com 4 indicadores (estáticos por enquanto). */
@Composable
fun StatusCard(dispositivo: Dispositivo, pessoa: PessoaAcompanhada, modifier: Modifier = Modifier) {
    val rows = listOf(
        Triple("Dispositivo", dispositivo.status, dispositivo.status == "Conectado"),
        Triple("Bateria", "${dispositivo.bateria}%", dispositivo.bateria > 20),
        Triple("Localização", if (dispositivo.localizacaoAtivada) "Ativada" else "Desativada", dispositivo.localizacaoAtivada),
        Triple("IA", if (dispositivo.iaAtivada) "Ativada" else "Desativada", dispositivo.iaAtivada),
    )
    Column(
        modifier.padding(horizontal = 18.dp).fillMaxWidth().shadow(10.dp, CardShape).clip(CardShape)
            .background(Brush.verticalGradient(listOf(WeSeeBlue, Navy))).padding(18.dp),
    ) {
        WeSeeText("Status de ${pessoa.nome}", Color.White.copy(alpha = 0.85f), 14, FontWeight.SemiBold, textAlign = TextAlign.Start)
        Spacer(Modifier.height(8.dp))
        rows.forEachIndexed { i, (label, value, ok) ->
            if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.25f)))
            StatusRow(label, value, ok)
        }
    }
}
