package com.wesee.app.maps

import com.google.android.gms.maps.model.LatLng
import com.wesee.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.math.roundToInt

data class Suggestion(val placeId: String, val primary: String, val secondary: String)
data class Place(val name: String, val address: String, val latLng: LatLng)
data class RouteResult(val points: List<LatLng>, val distanceMeters: Int, val durationSeconds: Int)

/**
 * Google Maps Platform via REST: Places API (New) para busca/detalhes e Routes API para a rota a pé.
 * A chave vem de BuildConfig.MAPS_API_KEY (variável de ambiente / local.properties, nunca do código).
 */
object MapsApi {
    val isConfigured: Boolean get() = BuildConfig.MAPS_API_KEY.isNotBlank()

    private suspend fun request(url: String, post: Boolean, body: String?, fieldMask: String?): String? = withContext(Dispatchers.IO) {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = if (post) "POST" else "GET"
            c.connectTimeout = 10000; c.readTimeout = 10000
            c.setRequestProperty("X-Goog-Api-Key", BuildConfig.MAPS_API_KEY)
            fieldMask?.let { c.setRequestProperty("X-Goog-FieldMask", it) }
            if (body != null) {
                c.setRequestProperty("Content-Type", "application/json")
                c.doOutput = true
                c.outputStream.use { it.write(body.toByteArray()) }
            }
            if (c.responseCode in 200..299) c.inputStream.bufferedReader().use { it.readText() } else null
        } catch (e: Exception) { null } finally { c.disconnect() }
    }

    private fun latLngJson(p: LatLng) = JSONObject().put("latitude", p.latitude).put("longitude", p.longitude)

    suspend fun autocomplete(input: String, near: LatLng?): List<Suggestion> {
        if (!isConfigured) return emptyList()
        val body = JSONObject().put("input", input).put("languageCode", "pt-BR").put("regionCode", "BR")
        near?.let {
            body.put("locationBias", JSONObject().put("circle", JSONObject().put("center", latLngJson(it)).put("radius", 30000.0)))
        }
        val text = request("https://places.googleapis.com/v1/places:autocomplete", true, body.toString(), null) ?: return emptyList()
        val arr = JSONObject(text).optJSONArray("suggestions") ?: return emptyList()
        return (0 until arr.length()).mapNotNull { i ->
            val p = arr.optJSONObject(i)?.optJSONObject("placePrediction") ?: return@mapNotNull null
            val sf = p.optJSONObject("structuredFormat")
            val main = sf?.optJSONObject("mainText")?.optString("text") ?: p.optJSONObject("text")?.optString("text") ?: return@mapNotNull null
            Suggestion(p.getString("placeId"), main, sf?.optJSONObject("secondaryText")?.optString("text").orEmpty())
        }
    }

    suspend fun placeDetails(placeId: String): Place? {
        if (!isConfigured) return null
        val text = request("https://places.googleapis.com/v1/places/$placeId?languageCode=pt-BR", false, null,
            "displayName,formattedAddress,location") ?: return null
        val o = JSONObject(text)
        val loc = o.optJSONObject("location") ?: return null
        return Place(
            o.optJSONObject("displayName")?.optString("text").orEmpty(),
            o.optString("formattedAddress"),
            LatLng(loc.getDouble("latitude"), loc.getDouble("longitude")),
        )
    }

    /** Rota a pé calculada pela Routes API (distância, tempo e traçado reais). */
    suspend fun walkingRoute(origin: LatLng, destination: LatLng): RouteResult? {
        if (!isConfigured) return null
        fun wp(p: LatLng) = JSONObject().put("location", JSONObject().put("latLng", latLngJson(p)))
        val body = JSONObject().put("origin", wp(origin)).put("destination", wp(destination))
            .put("travelMode", "WALK").put("polylineEncoding", "GEO_JSON_LINESTRING")
            .put("languageCode", "pt-BR").put("units", "METRIC")
        val text = request("https://routes.googleapis.com/directions/v2:computeRoutes", true, body.toString(),
            "routes.duration,routes.distanceMeters,routes.polyline.geoJsonLinestring") ?: return null
        val r = JSONObject(text).optJSONArray("routes")?.optJSONObject(0) ?: return null
        val coords = r.optJSONObject("polyline")?.optJSONObject("geoJsonLinestring")?.optJSONArray("coordinates") ?: return null
        val points = (0 until coords.length()).map { i -> coords.getJSONArray(i).let { LatLng(it.getDouble(1), it.getDouble(0)) } }
        val seconds = r.optString("duration", "0s").removeSuffix("s").toDoubleOrNull()?.roundToInt() ?: 0
        return RouteResult(points, r.optInt("distanceMeters"), seconds)
    }
}

fun formatDistance(meters: Int): String =
    if (meters < 1000) "$meters m" else String.format(Locale("pt", "BR"), "%.1f km", meters / 1000.0)

fun formatDuration(seconds: Int): String {
    val min = maxOf(1, (seconds / 60.0).roundToInt())
    return if (min < 60) "~$min min" else "~${min / 60} h ${min % 60} min"
}
