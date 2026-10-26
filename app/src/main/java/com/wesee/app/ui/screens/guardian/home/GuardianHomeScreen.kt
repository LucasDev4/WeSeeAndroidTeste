package com.wesee.app.ui.screens.guardian.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wesee.app.data.GuardianDemoData
import com.wesee.app.data.firstNameOf
import com.wesee.app.ui.screens.guardian.components.*
import com.wesee.app.ui.theme.WeSeeCard

/** Home do Responsável (dados demonstrativos). O sino abre o painel de notificações. */
@Composable
fun GuardianHomeScreen(responsavelName: String, onOpenMaps: () -> Unit = {}, onAccount: (() -> Unit)? = null) {
    val data = remember(responsavelName) { GuardianDemoData.build(responsavelName) }
    var panelOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = panelOpen) { panelOpen = false }

    Box(Modifier.fillMaxSize().background(WeSeeCard)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).statusBarsPadding().padding(top = 14.dp, bottom = 16.dp)) {
                GuardianHeader(greetingName = firstNameOf(responsavelName), pessoaNome = data.pessoa.nome, hasUnread = data.notificacoes.any { !it.lida }, onBellClick = { panelOpen = !panelOpen })
                Spacer(Modifier.height(6.dp))
                DeviceConnectedIndicator()
                Spacer(Modifier.height(8.dp))
                LocationSummaryCard(data.pessoa)
                Spacer(Modifier.height(18.dp))
                AlertsTitle()
                AlertsCarousel(data.alertas)
                Spacer(Modifier.height(16.dp))
                StatusCard(data.dispositivo, data.pessoa)
                Spacer(Modifier.height(16.dp))
                MapThumbnail(data.pessoa.latitude, data.pessoa.longitude, onClick = onOpenMaps)
            }
            GuardianBottomBar(GuardianTab.Home, onHome = {}, onMaps = onOpenMaps, onAccount = onAccount)
        }
        NotificationsPanelOverlay(panelOpen, data.notificacoes) { panelOpen = false }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable private fun GuardianHomePreview() = GuardianHomeScreen("Lucas")
