package com.wesee.app.ui.screens.guardian.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.wesee.app.maps.MapsApi
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.theme.WeSeeBlue
import kotlinx.coroutines.delay

/** Mapa ilustrativo (fallback visual) usado enquanto não há chave do Google Maps. NÃO é um mapa real. */
@Composable
private fun IllustrativeMap(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        drawRect(Color(0xFFE9EAEC))
        val park = Color(0xFFA8EDA0)
        listOf(
            floatArrayOf(0.04f, 0.06f, 0.26f, 0.2f), floatArrayOf(0.62f, 0.05f, 0.2f, 0.18f), floatArrayOf(0.06f, 0.55f, 0.22f, 0.22f),
            floatArrayOf(0.7f, 0.62f, 0.22f, 0.25f), floatArrayOf(0.34f, 0.72f, 0.18f, 0.18f),
        ).forEach { r -> drawRoundRect(park, Offset(w * r[0], h * r[1]), Size(w * r[2], h * r[3]), CornerRadius(14.dp.toPx())) }
        for (i in 1..6) {
            drawLine(Color.White, Offset(w * i / 7f, 0f), Offset(w * i / 7f + w * 0.08f, h), 2.dp.toPx())
            drawLine(Color.White, Offset(0f, h * i / 7f), Offset(w, h * i / 7f - h * 0.05f), 2.dp.toPx())
        }
        val road = Color(0xFFF7C26B)
        drawLine(road, Offset(0f, h * 0.2f), Offset(w, h * 0.55f), 5.dp.toPx())
        drawLine(road, Offset(w * 0.3f, 0f), Offset(w * 0.55f, h), 5.dp.toPx())
        drawLine(road, Offset(0f, h * 0.75f), Offset(w * 0.8f, h * 0.3f), 4.dp.toPx())
        val river = Path().apply { moveTo(w * 0.5f, h); cubicTo(w * 0.78f, h * 0.8f, w * 0.7f, h * 0.45f, w, h * 0.22f) }
        drawPath(river, Color(0xFF5CB8F0), style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
        val c = Offset(w * 0.45f, h * 0.5f)
        drawCircle(Color.Black, 11.dp.toPx(), c)
        drawCircle(WeSeeBlue, 8.dp.toPx(), c)
    }
}

/**
 * Mapa reutilizável do Responsável.
 * - Com MAPS_API_KEY configurada: Google Maps SDK real, câmera centrada em [latitude]/[longitude], marcador na posição
 *   (atualiza quando as coordenadas mudam), estado de carregamento e fallback se o mapa não carregar.
 * - Sem chave: mapa ILUSTRATIVO, com aviso. Onde configurar a chave: README → "Mapa (Google Maps Platform)".
 * Os dados (coordenadas) ficam fora daqui: este componente só apresenta.
 */
@Composable
fun GuardianMap(
    latitude: Double, longitude: Double, modifier: Modifier = Modifier,
    interactive: Boolean = true, markerTitle: String = "Localização demonstrativa", onClick: (() -> Unit)? = null,
) {
    val target = LatLng(latitude, longitude)
    val shape = RoundedCornerShape(22.dp)
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val useReal = MapsApi.isConfigured && !failed
    LaunchedEffect(useReal) {
        if (useReal) { delay(12000); if (!loaded) failed = true }
    }
    Box(modifier.shadow(10.dp, shape).clip(shape)) {
        if (useReal) {
            val camera = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(target, 15f) }
            LaunchedEffect(target) { camera.animate(CameraUpdateFactory.newLatLngZoom(target, 15f)) }
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = camera,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = interactive,
                    zoomGesturesEnabled = interactive,
                    rotationGesturesEnabled = interactive,
                    tiltGesturesEnabled = interactive,
                ),
                onMapLoaded = { loaded = true },
            ) {
                Marker(
                    state = remember(target) { MarkerState(target) },
                    title = markerTitle,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                )
            }
            if (!loaded) Box(Modifier.fillMaxSize().background(Color(0xFFE9EAEC)), contentAlignment = Alignment.Center) {
                WeSeeText("Carregando mapa...", WeSeeBlue, 11, FontWeight.SemiBold)
            }
        } else {
            IllustrativeMap(Modifier.fillMaxSize())
        }
        // aviso: os dados são demonstrativos / o mapa pode ser ilustrativo
        val note = when {
            MapsApi.isConfigured && failed -> "Mapa indisponível · localização demonstrativa"
            !MapsApi.isConfigured -> "Mapa ilustrativo · localização demonstrativa"
            else -> "Localização demonstrativa"
        }
        Box(Modifier.align(Alignment.BottomStart).padding(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 3.dp)) { WeSeeText(note, WeSeeBlue, 7, FontWeight.SemiBold) }
        if (onClick != null) {
            Box(Modifier.fillMaxSize().clickable(role = Role.Button, onClickLabel = "Abrir localização", onClick = onClick))
        }
    }
}

/** Miniatura do mapa da Home (toque abre a tela de Localização). */
@Composable
fun MapThumbnail(latitude: Double, longitude: Double, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GuardianMap(latitude, longitude, modifier.padding(horizontal = 18.dp).fillMaxWidth().height(110.dp), interactive = false, onClick = onClick)
}
