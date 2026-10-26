package com.wesee.app.ui.screens.pcd.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.ui.components.softCard

/**
 * Área da câmera. HOJE: apenas um placeholder visual.
 * FUTURO (TCC): passe um [content] com o preview real (CameraX + detecção de obstáculos) —
 * o restante da Home não precisa mudar. Ao ligar a câmera real, remova também o `rotate`.
 */
@Composable
fun CameraArea(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = { CameraPlaceholder() },
) {
    Box(
        modifier.fillMaxWidth().height(250.dp).rotate(1.2f).softCard(26.dp),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun BoxScope.CameraPlaceholder() {
    Image(painterResource(R.drawable.ic_camera), "Câmera, disponível em breve", Modifier.size(42.dp))
}
