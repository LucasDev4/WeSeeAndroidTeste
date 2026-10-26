package com.wesee.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeDeep

val SheetShape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
val SheetBrush = Brush.verticalGradient(listOf(WeSeeBlue, WeSeeDeep))

/** Painel azul inferior (bottom sheet) com rolagem para telas pequenas / teclado aberto. */
@Composable
fun WeSeeBottomPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(SheetShape).background(SheetBrush)
            .navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 16.dp),
        content = content,
    )
}
