package com.wesee.app.ui.screens.pcd.maps

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.wesee.app.R
import com.wesee.app.location.hasLocationPermission
import com.wesee.app.location.rememberUserLocation
import com.wesee.app.maps.MapsApi
import com.wesee.app.maps.Place
import com.wesee.app.maps.RouteResult
import com.wesee.app.maps.Suggestion
import com.wesee.app.maps.formatDistance
import com.wesee.app.maps.formatDuration
import com.wesee.app.ui.components.LocalPcdNav
import com.wesee.app.ui.components.PcdBottomBar
import com.wesee.app.ui.components.PcdTab
import com.wesee.app.ui.components.WeSeeText
import com.wesee.app.ui.components.softCard
import com.wesee.app.ui.theme.HostGrotesk
import com.wesee.app.ui.theme.WeSeeBlue
import com.wesee.app.ui.theme.WeSeeCard
import com.wesee.app.ui.theme.WeSeeMid
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ButtonText = Color.White.copy(alpha = 0.6f)
private val RouteColor = Color(0xFF7FB4F9)

@Composable
private fun PillButton(label: String, icon: Int, iconWidth: Int) {
    Row(
        Modifier.clip(RoundedCornerShape(50)).background(WeSeeBlue).clickable(role = Role.Button) { /* FUTURO */ }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.width(iconWidth.dp))
        Spacer(Modifier.width(8.dp))
        WeSeeText(label, ButtonText, 12, FontWeight.SemiBold)
    }
}

@Composable
private fun SearchCard(
    query: String, onQuery: (String) -> Unit, onClear: () -> Unit,
) {
    val textStyle = TextStyle(color = WeSeeBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = HostGrotesk)
    Row(Modifier.fillMaxWidth().softCard(24.dp).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_pin_add), null, Modifier.height(15.dp))
            Box(Modifier.width(1.dp).height(18.dp).background(WeSeeMid))
            Image(painterResource(R.drawable.ic_pin_add), null, Modifier.height(15.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth().height(34.dp), contentAlignment = Alignment.CenterStart) {
                WeSeeText("Sua localização", WeSeeMid, 12, FontWeight.SemiBold, textAlign = TextAlign.Start)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(WeSeeMid.copy(alpha = 0.5f)))
            Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = query, onValueChange = onQuery, singleLine = true,
                    textStyle = textStyle, cursorBrush = SolidColor(WeSeeBlue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) BasicText("Destino", style = textStyle.copy(color = WeSeeBlue.copy(alpha = 0.7f)))
                            inner()
                        }
                    },
                )
                if (query.isNotEmpty()) {
                    Box(Modifier.size(34.dp).clickable(role = Role.Button, onClick = onClear), contentAlignment = Alignment.Center) {
                        WeSeeText("✕", WeSeeMid, 14, FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionList(items: List<Suggestion>, onPick: (Suggestion) -> Unit) {
    Column(Modifier.fillMaxWidth().softCard(18.dp)) {
        items.take(5).forEach { s ->
            Column(Modifier.fillMaxWidth().clickable(role = Role.Button) { onPick(s) }.padding(horizontal = 16.dp, vertical = 10.dp)) {
                WeSeeText(s.primary, WeSeeBlue, 13, FontWeight.Bold, textAlign = TextAlign.Start)
                if (s.secondary.isNotBlank()) WeSeeText(s.secondary, WeSeeMid, 10, textAlign = TextAlign.Start)
            }
        }
    }
}

/** Localização do PCD: mapa real (Google Maps), busca de destino (Places) e rota a pé (Routes). */
@Composable
fun PcdMapsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val nav = LocalPcdNav.current

    var hasPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermission = context.hasLocationPermission()
    }
    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    val user = rememberUserLocation(hasPermission).value

    var query by rememberSaveable { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<Suggestion>()) }
    var destination by remember { mutableStateOf<Place?>(null) }
    var route by remember { mutableStateOf<RouteResult?>(null) }
    var routing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val camera = rememberCameraPositionState()
    var mapLoaded by remember { mutableStateOf(false) }
    var centered by remember { mutableStateOf(false) }
    val padPx = with(LocalDensity.current) { 90.dp.roundToPx() }
    val mapStyle = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) }

    // busca de destino (com debounce)
    LaunchedEffect(query) {
        if (query.length < 3 || query == destination?.name) { suggestions = emptyList(); return@LaunchedEffect }
        delay(350)
        suggestions = MapsApi.autocomplete(query, user)
    }
    // rota real: origem = posição atual, destino = lugar escolhido
    LaunchedEffect(destination, user != null) {
        val dest = destination
        if (dest == null) { route = null; return@LaunchedEffect }
        val origin = user ?: return@LaunchedEffect
        routing = true; error = null
        route = MapsApi.walkingRoute(origin, dest.latLng)
        if (route == null) error = "Não foi possível calcular a rota. Verifique a internet e a chave do Google Maps."
        routing = false
    }
    // centraliza na posição atual (uma vez)
    LaunchedEffect(user, mapLoaded) {
        if (user != null && mapLoaded && !centered && destination == null) {
            camera.animate(CameraUpdateFactory.newLatLngZoom(user, 16f)); centered = true
        }
    }
    // ajusta o zoom para mostrar a rota inteira
    LaunchedEffect(route, mapLoaded) {
        val r = route
        if (r != null && mapLoaded && r.points.size > 1) {
            val b = LatLngBounds.builder()
            r.points.forEach { b.include(it) }
            user?.let { b.include(it) }
            try { camera.animate(CameraUpdateFactory.newLatLngBounds(b.build(), padPx)) } catch (e: IllegalStateException) { }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF0B1622))) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            GoogleMap(
                Modifier.fillMaxSize(), cameraPositionState = camera,
                properties = MapProperties(isMyLocationEnabled = hasPermission, mapStyleOptions = mapStyle),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false, mapToolbarEnabled = false),
                contentPadding = PaddingValues(bottom = 230.dp),
                onMapLoaded = { mapLoaded = true },
            ) {
                destination?.let { d -> Marker(state = remember(d) { MarkerState(d.latLng) }, title = d.name) }
                route?.let { Polyline(points = it.points, color = RouteColor, width = 14f) }
            }

            Column(Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchCard(query, { query = it }, onClear = { query = ""; destination = null; route = null; suggestions = emptyList(); error = null })
                if (suggestions.isNotEmpty()) {
                    SuggestionList(suggestions) { s ->
                        scope.launch {
                            val place = MapsApi.placeDetails(s.placeId)
                            if (place != null) {
                                destination = place; query = place.name; suggestions = emptyList(); focus.clearFocus()
                            } else error = "Não consegui abrir esse local."
                        }
                    }
                }
                if (!hasPermission) {
                    Row(Modifier.fillMaxWidth().softCard(18.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        WeSeeText("Ative a localização para ver sua posição no mapa.", WeSeeBlue, 11, FontWeight.SemiBold,
                            Modifier.weight(1f), textAlign = TextAlign.Start)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(50)).background(WeSeeBlue).clickable(role = Role.Button) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }.padding(horizontal = 14.dp, vertical = 8.dp)) { WeSeeText("Permitir", ButtonText, 11, FontWeight.SemiBold) }
                    }
                }
                if (!MapsApi.isConfigured) {
                    Box(Modifier.fillMaxWidth().softCard(18.dp).padding(12.dp)) {
                        WeSeeText("Chave do Google Maps não configurada (MAPS_API_KEY).", WeSeeBlue, 11, FontWeight.SemiBold, textAlign = TextAlign.Start)
                    }
                }
            }

            // card inferior: destino + distância/tempo reais
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .background(Brush.verticalGradient(0f to Color.Transparent, 0.3f to WeSeeCard, 1f to WeSeeCard))
                    .padding(start = 20.dp, end = 20.dp, top = 64.dp, bottom = 14.dp),
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    WeSeeText(destination?.name ?: "Selecione um destino", WeSeeBlue, 24, FontWeight.Bold, Modifier.weight(1f),
                        lineHeight = 28, textAlign = TextAlign.Start)
                    val r = route
                    if (r != null) {
                        Spacer(Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            WeSeeText(formatDistance(r.distanceMeters), WeSeeBlue, 22, FontWeight.Bold)
                            WeSeeText(formatDuration(r.durationSeconds), WeSeeMid, 12, FontWeight.SemiBold)
                        }
                    } else if (routing) {
                        WeSeeText("Calculando...", WeSeeMid, 12, FontWeight.SemiBold)
                    }
                }
                error?.let { WeSeeText(it, WeSeeMid, 10, textAlign = TextAlign.Start, modifier = Modifier.padding(top = 4.dp)) }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PillButton("Direção", R.drawable.ic_direction, 15)
                    Spacer(Modifier.width(10.dp))
                    PillButton("Rotas", R.drawable.ic_arrow_left, 16)
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier.size(78.dp).shadow(8.dp, CircleShape).clip(CircleShape).background(WeSeeBlue)
                            .clickable(role = Role.Button) { /* FUTURO: iniciar navegação */ },
                        contentAlignment = Alignment.Center,
                    ) { Image(painterResource(R.drawable.ic_turn_right), "Iniciar navegação", Modifier.size(40.dp)) }
                }
            }
        }
        PcdBottomBar(PcdTab.Maps, onHome = nav.onHome, onChat = nav.onChat)
    }
}
