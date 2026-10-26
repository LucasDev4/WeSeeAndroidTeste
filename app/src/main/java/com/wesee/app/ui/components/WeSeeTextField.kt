package com.wesee.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wesee.app.R
import com.wesee.app.ui.theme.HostGrotesk
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard

/** Campo claro com ícone à esquerda; com [isPassword] ganha o botão de mostrar/ocultar senha. */
@Composable
fun WeSeeTextField(
    @DrawableRes icon: Int,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
) {
    var visible by remember { mutableStateOf(false) }
    val textStyle = TextStyle(color = WeSeeBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = HostGrotesk)
    Row(
        modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(WeSeeCard)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value, onValueChange = onValueChange, singleLine = true,
            textStyle = textStyle, cursorBrush = SolidColor(WeSeeBlue),
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) androidx.compose.foundation.text.BasicText(placeholder, style = textStyle)
                    inner()
                }
            },
        )
        if (isPassword) {
            Image(
                painterResource(if (visible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                stringResource(if (visible) R.string.hide_password else R.string.show_password),
                Modifier.size(20.dp).clickable { visible = !visible },
            )
        }
    }
}
