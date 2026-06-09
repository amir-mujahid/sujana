package com.sujana.data.repository

import com.google.android.gms.maps.model.LatLng
import com.sujana.BuildConfig
import com.sujana.core.common.AppError
import com.sujana.core.common.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

data class RouteInfo(val points: List<LatLng>, val distanceText: String)

@Singleton
class DirectionsRepository @Inject constructor(
    @Named("cloudinary") private val httpClient: OkHttpClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getRoute(
        originLat: Double, originLng: Double,
        destLat: Double, destLng: Double,
    ): AppResult<RouteInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=$originLat,$originLng" +
                    "&destination=$destLat,$destLng" +
                    "&key=${BuildConfig.MAPS_API_KEY}"
            val request = Request.Builder().url(url).get().build()
            val body = httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) error("Directions API error: ${resp.code}")
                resp.body?.string() ?: error("Empty body")
            }
            val response = json.decodeFromString<DirectionsResponse>(body)
            val route = response.routes.firstOrNull() ?: error("No routes found")
            val points = decodePolyline(route.overviewPolyline.points)
            val distanceText = route.legs.firstOrNull()?.distance?.text ?: ""
            RouteInfo(points = points, distanceText = distanceText)
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(AppError.Network(it.message ?: "Directions error", it)) },
        )
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val result = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var acc = 0
            do {
                b = encoded[index++].code - 63
                acc = acc or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lat += if (acc and 1 != 0) (acc shr 1).inv() else acc shr 1
            shift = 0
            acc = 0
            do {
                b = encoded[index++].code - 63
                acc = acc or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            lng += if (acc and 1 != 0) (acc shr 1).inv() else acc shr 1
            result.add(LatLng(lat / 1e5, lng / 1e5))
        }
        return result
    }
}

@Serializable
private data class DirectionsResponse(
    val routes: List<DirectionsRoute> = emptyList(),
    val status: String = "",
)

@Serializable
private data class DirectionsRoute(
    @SerialName("overview_polyline") val overviewPolyline: EncodedPolyline,
    val legs: List<DirectionsLeg> = emptyList(),
)

@Serializable
private data class EncodedPolyline(val points: String = "")

@Serializable
private data class DirectionsLeg(val distance: DirectionsDistance? = null)

@Serializable
private data class DirectionsDistance(val text: String = "")
