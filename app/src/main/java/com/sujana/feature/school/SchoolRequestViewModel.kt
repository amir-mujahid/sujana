package com.sujana.feature.school

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.sujana.core.cloudinary.CloudinaryUploader
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.request.CreatePickupRequest
import com.sujana.shared.RequestType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class SchoolRequestFormState(
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val pickupAddress: String = "",
    val locationError: String? = null,
    val scheduledDate: LocalDate? = null,
    val scheduledTime: LocalTime? = null,
    val notes: String = "",
    val photoUri: Uri? = null,
    val photoUrl: String? = null,
    val isUploadingPhoto: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
) {
    val scheduledLabel: String get() = when {
        scheduledDate != null && scheduledTime != null ->
            "${scheduledDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} at ${scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
        scheduledDate != null ->
            scheduledDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        else -> ""
    }

    fun toIso8601(): String? {
        val date = scheduledDate ?: return null
        val time = scheduledTime ?: LocalTime.of(9, 0)
        return OffsetDateTime.of(date, time, ZoneId.systemDefault().rules.getOffset(java.time.Instant.now()))
            .toString()
    }
}

@HiltViewModel
class SchoolRequestViewModel @Inject constructor(
    application: Application,
    private val createPickupRequest: CreatePickupRequest,
    private val cloudinaryUploader: CloudinaryUploader,
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _form = MutableStateFlow(SchoolRequestFormState())
    val form: StateFlow<SchoolRequestFormState> = _form.asStateFlow()

    private val _submitSuccess = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val submitSuccess: SharedFlow<String> = _submitSuccess.asSharedFlow()

    fun onMapTap(lat: Double, lng: Double) {
        viewModelScope.launch {
            _form.update { it.copy(pickupLat = lat, pickupLng = lng, locationError = null) }
            val address = reverseGeocode(lat, lng)
            _form.update { it.copy(pickupAddress = address) }
        }
    }

    fun onUseCurrentLocation() {
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(ctx)
                val location = client.lastLocation.await()
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    _form.update { it.copy(pickupLat = lat, pickupLng = lng, locationError = null) }
                    val address = reverseGeocode(lat, lng)
                    _form.update { it.copy(pickupAddress = address) }
                } else {
                    _form.update { it.copy(locationError = "Could not get current location.") }
                }
            } catch (e: SecurityException) {
                _form.update { it.copy(locationError = "Location permission required.") }
            } catch (e: Exception) {
                _form.update { it.copy(locationError = "Could not get location: ${e.message}") }
            }
        }
    }

    fun onScheduledDateSelected(date: LocalDate?) {
        _form.update { it.copy(scheduledDate = date) }
    }

    fun onScheduledTimeSelected(time: LocalTime?) {
        _form.update { it.copy(scheduledTime = time) }
    }

    fun onNotesChange(value: String) {
        _form.update { it.copy(notes = value) }
    }

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _form.update { it.copy(photoUri = uri, isUploadingPhoto = true, photoUrl = null) }
            try {
                val tempId = "temp_${System.currentTimeMillis()}"
                val url = cloudinaryUploader.uploadImage(uri, "sujana/requests/$tempId")
                _form.update { it.copy(photoUrl = url, isUploadingPhoto = false) }
            } catch (e: Exception) {
                _form.update { it.copy(isUploadingPhoto = false, photoUri = null, photoUrl = null) }
            }
        }
    }

    fun onRemovePhoto() {
        _form.update { it.copy(photoUri = null, photoUrl = null, isUploadingPhoto = false) }
    }

    fun submit() {
        val f = _form.value
        if (f.pickupLat == null || f.pickupLng == null) {
            _form.update { it.copy(locationError = "Please set the pickup location on the map.") }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(isSubmitting = true, submitError = null) }
            when (val result = createPickupRequest(
                pickupLat    = f.pickupLat,
                pickupLng    = f.pickupLng,
                pickupAddress = f.pickupAddress,
                notes        = f.notes.ifBlank { null },
                photoUrl     = f.photoUrl,
                type         = RequestType.SCHOOL,
                scheduledFor = f.toIso8601(),
            )) {
                is AppResult.Success -> {
                    _form.update { it.copy(isSubmitting = false) }
                    _submitSuccess.emit(result.data.id)
                }
                is AppResult.Error -> _form.update {
                    it.copy(isSubmitting = false, submitError = result.error.toString())
                }
            }
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext "$lat, $lng"
                @Suppress("DEPRECATION")
                val addresses = Geocoder(ctx, Locale.getDefault()).getFromLocation(lat, lng, 1)
                addresses?.firstOrNull()?.getAddressLine(0) ?: "$lat, $lng"
            } catch (e: Exception) {
                "$lat, $lng"
            }
        }
}
