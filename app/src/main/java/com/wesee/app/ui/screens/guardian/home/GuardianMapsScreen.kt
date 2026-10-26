package com.wesee.app.ui.screens.guardian.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wesee.app.data.GuardianDemoData
import com.wesee.app.data.firstNameOf
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.screens.guardian.components.*
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard

@Composable
private fun LocationTitle() {
    val chip = RoundedCornerShape(14.dp)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.shadow(6.dp, chip).clip(chip).background(Color(0xFF8FC9FF)).padding(horizontal = 10.dp, vertical = 2.dp)) {
                WeSeeText("Localização", WeSeeBlue, 28, FontWeight.SemiBold)
            }
            Spacer(Modifier.width(8.dp))
            WeSeeText("Atual", WeSeeBlue, 28, FontWeight.SemiBold)
        }
        WeSeeText("do Menor", WeSeeBlue, 28, FontWeight.SemiBold)
    }
}

/** Tela de Localização do Responsável: mesmo cabeçalho/card da Home + mapa amplo. */
@Composable
fun GuardianMapsScreen(responsavelName: String, onOpenHome: () -> Unit = {}, onAccount: (() -> Unit)? = null) {
    val data = remember(responsavelName) { GuardianDemoData.build(responsavelName) }
    var panelOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = panelOpen) { panelOpen = false }

    Box(Modifier.fillMaxSize().background(WeSeeCard)) {
        Column(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val mapHeight = (maxHeight - 380.dp).coerceAtLeast(260.dp)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(top = 14.dp, bottom = 16.dp)) {
                    GuardianHeader(greetingName = firstNameOf(responsavelName), pessoaNome = data.pessoa.nome, hasUnread = data.notificacoes.any { !it.lida }, onBellClick = { panelOpen = !panelOpen })
                    Spacer(Modifier.height(6.dp))
                    DeviceConnectedIndicator()
                    Spacer(Modifier.height(8.dp))
                    LocationSummaryCard(data.pessoa)
                    Spacer(Modifier.height(22.dp))
                    LocationTitle()
                    Spacer(Modifier.height(16.dp))
                    GuardianMap(
                        data.pessoa.latitude, data.pessoa.longitude,
                        Modifier.padding(horizontal = 18.dp).fillMaxWidth().height(mapHeight),
                        markerTitle = "Localização demonstrativa de ${data.pessoa.nome}",
                    )
                }
            }
            GuardianBottomBar(GuardianTab.Maps, onHome = onOpenHome, onMaps = {}, onAccount = onAccount)
        }
        NotificationsPanelOverlay(panelOpen, data.notificacoes) { panelOpen = false }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable private fun GuardianMapsPreview() = GuardianMapsScreen("Lucas")
