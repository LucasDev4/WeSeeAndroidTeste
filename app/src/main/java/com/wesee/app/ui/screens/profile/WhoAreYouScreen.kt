package com.wesee.app.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.components.*
import com.wesee.app.ui.theme.WeSeeCard

@Composable
private fun Choice(icon: Int, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(108.dp).clip(RoundedCornerShape(14.dp)).background(WeSeeCard).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Image(painterResource(icon), label, Modifier.size(58.dp)) }
        Spacer(Modifier.height(12.dp))
        WeSeeText(label, Color.White, 12)
    }
}

/** Tela 2 — Quem é você? */
@Composable
fun WhoAreYouScreen(onSelectPcd: () -> Unit = {}, onSelectGuardian: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().background(WeSeeCard)) {
        Column(Modifier.weight(0.6f).fillMaxWidth().statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            WeSeeHeader(1)
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { WeSeeEyes() }
        }
        Column(
            Modifier.weight(0.4f).fillMaxWidth().clip(SheetShape).background(SheetBrush).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(22.dp))
            WeSeeText("Quem é você?", Color.White, 34, FontWeight.SemiBold)
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp), verticalAlignment = Alignment.Top) {
                Choice(R.drawable.ic_pcd_cane, "PCD visual", onSelectPcd)
                Choice(R.drawable.ic_binoculars, "Responsável do PCD", onSelectGuardian)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable private fun WhoAreYouPreview() = WhoAreYouScreen()
