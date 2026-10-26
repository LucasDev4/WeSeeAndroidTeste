package com.wesee.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

/** Posição real do aparelho, atualizada enquanto o composable estiver na tela. Só use com [enabled] = permissão concedida. */
@SuppressLint("MissingPermission")
@Composable
fun rememberUserLocation(enabled: Boolean): State<LatLng?> {
    val context = LocalContext.current
    val state = remember { mutableStateOf<LatLng?>(null) }
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { l ->
            if (l != null && state.value == null) state.value = LatLng(l.latitude, l.longitude)
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).setMinUpdateDistanceMeters(5f).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { state.value = LatLng(it.latitude, it.longitude) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        onDispose { client.removeLocationUpdates(callback) }
    }
    return state
}

/** "Cidade - Estado, País" a partir da posição (null se indisponível). */
suspend fun reverseGeocode(context: Context, point: LatLng): String? = withContext(Dispatchers.IO) {
    try {
        if (!Geocoder.isPresent()) return@withContext null
        @Suppress("DEPRECATION")
        val a = Geocoder(context, Locale("pt", "BR")).getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
            ?: return@withContext null
        val region = listOfNotNull(a.locality ?: a.subAdminArea, a.adminArea).distinct().joinToString(" - ")
        listOfNotNull(region.ifBlank { null }, a.countryName).joinToString(", ").ifBlank { null }
    } catch (e: Exception) { null }
}
