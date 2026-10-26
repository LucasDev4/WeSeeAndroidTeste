package com.wesee.app.ui.screens.links

import android.content.Intent
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wesee.app.network.VinculoItem
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.screens.guardian.components.StatusGreen
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard
import com.wesee.app.ui.theme.WeSeeMid

private val SoftBlue = Color(0xFF8FC9FF)
private val Amber = Color(0xFFFFB300)
private val ErrorText = Color(0xFFB3261E)

@Composable
private fun Spinner() {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart), label = "angle")
    Canvas(Modifier.size(28.dp).rotate(angle)) {
        drawArc(WeSeeBlue, 0f, 100f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(42.dp).alpha(if (enabled) 1f else 0.5f).clip(RoundedCornerShape(12.dp))
            .background(WeSeeBlue).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { WeSeeText(label, Color.White, 14, FontWeight.Bold) }
}

/** Botão secundário pequeno (área de toque mínima de 48dp). */
@Composable
private fun SmallButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.heightIn(min = 48.dp).alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 14.dp, vertical = 7.dp)) {
            WeSeeText(label, WeSeeBlue, 12, FontWeight.Bold)
        }
    }
}

/** Mensagem que o TalkBack anuncia uma única vez quando o texto muda. */
@Composable
private fun LiveMessage(text: String, color: Color) {
    WeSeeText(text, color, 12, FontWeight.SemiBold, Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite })
}

private fun statusLabel(status: String) = when (status) {
    "ATIVO" -> "Vínculo ativo"
    "PENDENTE" -> "Aguardando confirmação"
    else -> status
}

@Composable
private fun LinkCard(item: VinculoItem) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(WeSeeBlue).padding(horizontal = 12.dp, vertical = 10.dp)
            .semantics(mergeDescendants = true) { contentDescription = "${item.nome}, ${statusLabel(item.status).lowercase()}" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            WeSeeText(item.nome, Color.White, 13, FontWeight.Bold, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            WeSeeText(statusLabel(item.status), Color.White.copy(alpha = 0.85f), 10, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
        }
        Spacer(Modifier.width(8.dp))
        // além do texto, o indicador de cor reforça o estado (nunca só a cor)
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (item.status == "ATIVO") StatusGreen else Amber))
    }
}

@Composable
private fun ListContent(c: LinksController) {
    WeSeeText(c.title, WeSeeBlue, 20, FontWeight.Bold, Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    Spacer(Modifier.height(10.dp))
    Box(Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 230.dp), contentAlignment = Alignment.Center) {
        val error = c.listError
        when {
            c.loadingList -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spinner(); Spacer(Modifier.height(6.dp)); WeSeeText("Carregando...", WeSeeMid, 12, FontWeight.SemiBold)
            }
            error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LiveMessage(error, ErrorText)
                SmallButton("Tentar novamente") { c.refresh() }
            }
            c.items.isEmpty() -> WeSeeText("Sem vínculo ainda...", WeSeeBlue, 13, FontWeight.SemiBold)
            else -> LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(c.items, key = { it.id }) { LinkCard(it) }
            }
        }
    }
    c.notice?.let {
        Spacer(Modifier.height(8.dp))
        LiveMessage(it, WeSeeBlue)
    }
    Spacer(Modifier.height(12.dp))
    PrimaryButton("Adicionar vínculo", enabled = !c.loadingList) { c.startAdd() }
}

/** Seis caixas (vazias = azul cheio; preenchidas mostram o dígito em branco). [active] destaca a próxima posição. */
@Composable
private fun CodeBoxes(value: String, active: Int = -1, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(VINCULO_CODE_LENGTH) { i ->
            Box(
                Modifier.weight(1f).height(46.dp).clip(shape).background(WeSeeBlue)
                    .then(if (i == active) Modifier.border(2.dp, SoftBlue, shape) else Modifier),
                contentAlignment = Alignment.Center,
            ) { if (i < value.length) WeSeeText(value[i].toString(), Color.White, 20, FontWeight.Bold) }
        }
    }
}

/** Campo do código: UM campo invisível recebe a digitação (teclado numérico, colar, foco automático) e as caixas o exibem. */
@Composable
private fun CodeInput(value: String, enabled: Boolean, onChange: (String) -> Unit, onDone: () -> Unit) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focus.requestFocus(); keyboard?.show() }
    val filled = value.length
    BasicTextField(
        value = value, onValueChange = onChange, enabled = enabled, singleLine = true,
        textStyle = TextStyle(color = Color.Transparent), cursorBrush = SolidColor(Color.Transparent),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focus).semantics {
            contentDescription = "Código de vinculação com $VINCULO_CODE_LENGTH dígitos"
            stateDescription = if (filled == VINCULO_CODE_LENGTH) "Todos os dígitos preenchidos"
            else "$filled de $VINCULO_CODE_LENGTH dígitos preenchidos, faltam ${VINCULO_CODE_LENGTH - filled}"
        },
        decorationBox = { inner ->
            Box {
                CodeBoxes(value, active = if (filled < VINCULO_CODE_LENGTH) filled else -1, modifier = Modifier.clearAndSetSemantics { })
                Box(Modifier.matchParentSize().alpha(0f)) { inner() }
            }
        },
    )
}

@Composable
private fun GuardianCodeContent(c: LinksController) {
    val clipboard = LocalClipboardManager.current
    WeSeeText("Insira o código", WeSeeBlue, 18, FontWeight.SemiBold, Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    CodeInput(c.input, enabled = !c.busy, onChange = c::onInput, onDone = { c.confirm() })
    c.codeError?.let { Spacer(Modifier.height(8.dp)); LiveMessage(it, ErrorText) }
    Spacer(Modifier.height(12.dp))
    PrimaryButton(if (c.busy) "Enviando..." else "Confirmar", enabled = c.input.length == VINCULO_CODE_LENGTH && !c.busy) { c.confirm() }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        SmallButton("Colar", enabled = !c.busy) { c.onInput(clipboard.getText()?.text.orEmpty()) }
        SmallButton("Voltar", enabled = !c.busy) { c.back() }
    }
}

@Composable
private fun PcdCodeContent(c: LinksController) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember(c.generated) { mutableStateOf(false) }
    val remaining by produceState(0L, c.expiresAtMs) {
        while (true) {
            value = ((c.expiresAtMs - SystemClock.elapsedRealtime()) / 1000).coerceAtLeast(0)
            kotlinx.coroutines.delay(1000)
        }
    }
    LaunchedEffect(c.generated) { if (c.generated != null) c.watchForUse() }

    WeSeeText("Código", WeSeeBlue, 18, FontWeight.SemiBold, Modifier.fillMaxWidth())
    WeSeeText("Código de vinculação para compartilhar com o responsável.", WeSeeMid, 10, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    val generated = c.generated
    val error = c.codeError
    when {
        c.busy -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spinner(); Spacer(Modifier.height(6.dp)); WeSeeText("Gerando código...", WeSeeMid, 12, FontWeight.SemiBold)
        }
        generated != null -> {
            val code = generated.codigo
            CodeBoxes(code, modifier = Modifier.clearAndSetSemantics {
                contentDescription = "Seu código de vinculação: ${code.toList().joinToString(", ")}"
            })
            Spacer(Modifier.height(8.dp))
            if (remaining > 0) {
                WeSeeText(String.format("Expira em %02d:%02d", remaining / 60, remaining % 60), WeSeeMid, 11, FontWeight.SemiBold, Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    SmallButton(if (copied) "Copiado" else "Copiar") { clipboard.setText(AnnotatedString(code)); copied = true }
                    SmallButton("Compartilhar") {
                        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, "Código de vinculação do WeSee: $code (válido por pouco tempo)")
                        context.startActivity(Intent.createChooser(send, "Compartilhar código"))
                    }
                }
                if (copied) LiveMessage("Código copiado.", WeSeeBlue)
            } else {
                LiveMessage("Código expirado.", ErrorText)
                Spacer(Modifier.height(8.dp))
                PrimaryButton("Gerar novo código") { c.generate() }
            }
        }
        error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            LiveMessage(error, ErrorText)
            SmallButton("Tentar novamente") { c.generate() }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        SmallButton("Voltar") { c.back() }
    }
}

/**
 * Modal de vínculos, desenhado sobre a tela de Perfil (que fica escurecida ao fundo).
 * Voltar: código → lista → fecha o modal. Nada de navegação nova.
 */
@Composable
fun LinksModal(c: LinksController) {
    BackHandler(enabled = c.step != LinksStep.Closed) { c.back() }
    if (c.step == LinksStep.Closed) return
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier.fillMaxSize().background(Color(0x99001A33))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { c.close() }
            .statusBarsPadding().navigationBarsPadding().imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxWidth(0.84f).shadow(16.dp, shape).clip(shape).background(WeSeeCard)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(18.dp)
                .semantics { paneTitle = if (c.step == LinksStep.List) c.title else "Código de vinculação" },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (c.step) {
                LinksStep.List -> ListContent(c)
                LinksStep.Code -> if (c.isGuardian) GuardianCodeContent(c) else PcdCodeContent(c)
                LinksStep.Closed -> Unit
            }
        }
    }
}
