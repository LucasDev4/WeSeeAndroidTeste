package com.wesee.app.ui.screens.profile

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesee.app.R
import com.wesee.app.data.UserStore
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ButtonText = Color.White.copy(alpha = 0.6f)

private fun decodeAvatar(path: String): ImageBitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 800) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
} catch (e: Exception) { null }

@Composable
private fun OptionRow(icon: Int, label: String, trailing: String? = null, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier.fillMaxWidth().height(40.dp).shadow(4.dp, shape).clip(shape).background(WeSeeBlue)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        WeSeeText(label, ButtonText, 11, FontWeight.Medium, Modifier.weight(1f), textAlign = TextAlign.Start)
        trailing?.let { WeSeeText(it, ButtonText, 8, modifier = Modifier.padding(end = 8.dp)) }
        Image(painterResource(R.drawable.ic_chevron), null, Modifier.height(14.dp))
    }
}

/** Opção extra do Perfil que abre o modal de vínculos ("Meus responsáveis" / "Pessoas vinculadas"). */
data class LinksOption(val label: String, val onClick: () -> Unit)

/**
 * Tela de Perfil compartilhada (PCD visual e Responsável): mesmo visual, textos e barra inferior vêm de quem chama.
 * Interativos: escolher foto (avatar / lápis) e Logout. As demais opções são apenas visuais.
 */
@Composable
fun ProfileContent(
    userStore: UserStore,
    locationText: String,
    relationText: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    showName: Boolean = true,
    linksOption: LinksOption? = null,
    overlay: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var avatarPath by remember { mutableStateOf(userStore.avatarPath) }
    var avatar by remember { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(avatarPath) {
        avatar = avatarPath?.let { withContext(Dispatchers.IO) { decodeAvatar(it) } }
    }
    // cancelar a seleção (uri == null) não altera a foto atual
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val saved = withContext(Dispatchers.IO) { userStore.saveAvatar(context, uri) }
            if (saved != null) avatarPath = saved
        }
    }
    val pickPhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    val displayName = userStore.name.trim()

    Box(Modifier.fillMaxSize().background(WeSeeCard)) {
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(0f, size.height * 0.42f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF6FA8E8).copy(alpha = 0.45f), Color.Transparent), c, size.width * 0.5f),
                radius = size.width * 0.5f, center = c)
        }
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 18.dp, vertical = 14.dp)) {
                Box(
                    Modifier.size(40.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(WeSeeBlue)
                        .clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { Image(painterResource(R.drawable.ic_arrow_left), "Voltar", Modifier.width(20.dp)) }
                Spacer(Modifier.height(10.dp))
                WeSeeText("Perfil", WeSeeBlue, 32, FontWeight.Bold, textAlign = TextAlign.Start)
                Spacer(Modifier.height(14.dp))

                val cardShape = RoundedCornerShape(22.dp)
                Box(Modifier.fillMaxWidth().shadow(10.dp, cardShape).clip(cardShape).background(WeSeeBlue).padding(18.dp)) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(116.dp, 96.dp).clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFFDDE8F4), Color(0xFFA9BCD0))))
                                .clickable(role = Role.Button, onClickLabel = "Escolher foto de perfil") { pickPhoto() },
                            contentAlignment = Alignment.Center,
                        ) {
                            val bmp = avatar
                            val description = if (displayName.isBlank()) "Foto de perfil" else "Foto de perfil de $displayName"
                            if (bmp != null) Image(bmp, description, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else WeSeeText(userStore.initial, WeSeeBlue, 36, FontWeight.Medium)
                        }
                        Spacer(Modifier.height(10.dp))
                        if (showName) {
                            WeSeeText(displayName.ifBlank { "Usuário" }, Color.White, 16, FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                        }
                        WeSeeText(locationText, Color.White.copy(alpha = 0.85f), 11, FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        WeSeeText(relationText, Color.White.copy(alpha = 0.85f), 10)
                        Spacer(Modifier.height(18.dp))
                    }
                    Box(
                        Modifier.align(Alignment.BottomStart).size(34.dp).clip(CircleShape).background(WeSeeCard)
                            .clickable(role = Role.Button, onClickLabel = "Editar foto") { pickPhoto() },
                        contentAlignment = Alignment.Center,
                    ) { Image(painterResource(R.drawable.ic_brush), "Editar foto", Modifier.size(16.dp)) }
                }

                Spacer(Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionRow(R.drawable.ic_opt_appearance, "Aparência")
                    linksOption?.let { OptionRow(R.drawable.ic_family, it.label, onClick = it.onClick) }
                    OptionRow(R.drawable.ic_opt_history, "Histórico")
                    OptionRow(R.drawable.ic_opt_language, "Idioma", trailing = "PT - BR")
                }
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionRow(R.drawable.ic_opt_notifications, "Notificações")
                    OptionRow(R.drawable.ic_opt_terms, "Termos de uso")
                    OptionRow(R.drawable.ic_opt_privacy, "Política de privacidade")
                    OptionRow(R.drawable.ic_opt_faq, "FAQ's")
                }
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OptionRow(R.drawable.ic_opt_delete, "Apagar conta")
                    OptionRow(R.drawable.ic_opt_logout, "Logout", onClick = onLogout)
                }
                Spacer(Modifier.height(8.dp))
            }
            bottomBar()
        }
        overlay()
    }
}
