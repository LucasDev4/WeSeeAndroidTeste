package com.wesee.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wesee.app.ui.components.*
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard

/** Frase de destaque; as aspas são elementos separados, acima e nas extremidades. */
@Composable
private fun Tagline() {
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        WeSeeText("“", WeSeeBlue, 60, FontWeight.ExtraBold, Modifier.align(Alignment.TopStart), 60)
        WeSeeText("”", WeSeeBlue, 60, FontWeight.ExtraBold, Modifier.align(Alignment.TopEnd), 60)
        WeSeeText("Tecnologia que guia.", WeSeeBlue, 32, FontWeight.ExtraBold,
            Modifier.align(Alignment.Center).padding(top = 26.dp))
    }
}

/** Tela 1 — Login / Cadastro. */
@Composable
fun WelcomeScreen(onLogin: () -> Unit = {}, onRegister: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().background(WeSeeCard).statusBarsPadding().navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WeSeeHeader(0)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { WeSeeEyes() }
        Tagline()
        Spacer(Modifier.height(10.dp))
        WeSeeText("O WeSee é um sistema que utiliza IA com câmera\npara identificar obstáculos.",
            Color(0xFF2B3A4A), 10, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(40.dp))
        AppButton("Login", primary = false, onClick = onLogin)
        Spacer(Modifier.height(10.dp))
        AppButton("Cadastro", primary = true, onClick = onRegister)
        Spacer(Modifier.height(28.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable private fun WelcomePreview() = WelcomeScreen()
