package com.wesee.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard

/** Card claro com sombra suave (cards da Home). */
fun Modifier.softCard(radius: Dp = 22.dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.shadow(8.dp, shape, ambientColor = WeSeeBlue.copy(alpha = 0.25f), spotColor = WeSeeBlue.copy(alpha = 0.25f))
        .clip(shape).background(WeSeeCard)
}

/** Faz o conteúdo subir [amount] sobre o elemento anterior (e encolhe o espaço ocupado). */
fun Modifier.overlapTop(amount: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val o = amount.roundToPx()
    layout(placeable.width, placeable.height - o) { placeable.place(0, -o) }
}
