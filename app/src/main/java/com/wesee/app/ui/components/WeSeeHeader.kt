package com.wesee.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.theme.WeSeeBar
import com.wesee.app.ui.theme.WeSeeBlue

/** Logo (imagem independente) + indicador de progresso de 3 etapas. */
@Composable
fun WeSeeHeader(step: Int, topPadding: Dp = 40.dp) {
    Column(Modifier.fillMaxWidth().padding(top = topPadding), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.wesee_logo), "WeSee", Modifier.height(34.dp))
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                Box(Modifier.size(54.dp, 8.dp).clip(RoundedCornerShape(50))
                    .background(if (it == step) WeSeeBlue else WeSeeBar))
            }
        }
    }
}

/** Olhinhos do WeSee (asset único). */
@Composable
fun WeSeeEyes(@DrawableRes image: Int = R.drawable.wesee_eyes) =
    Image(painterResource(image), null, Modifier.width(220.dp))
