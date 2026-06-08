package com.sujana.feature.request

import android.app.Application
import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.sujana.core.cloudinary.CloudinaryUploader
import com.sujana.core.common.AppResult
import com.sujana.domain.model.School
import com.sujana.domain.usecase.request.CreatePickupRequest
import com.sujana.domain.usecase.request.GetSchools
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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateRequestViewModel @Inject constructor(
    application: Application,
    private val createPickupRequest: CreatePickupRequest,
    private val getSchools: GetSchools,
    private val cloudinaryUploader: CloudinaryUploader,
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _form = MutableStateFlow(CreateRequestFormState())
    val form: StateFlow<CreateRequestFormState> = _form.asStateFlow()

    private val _submitSuccess = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val submitSuccess: SharedFlow<String> = _submitSuccess.asSharedFlow()

    init {
        loadSchools()
    }

    private fun loadSchools() {
        viewModelScope.launch {
            _form.update { it.copy(schoolsLoading = true) }
            when (val result = getSchools()) {
                is AppResult.Success -> _form.update { it.copy(schools = result.data, schoolsLoading = false) }
                is AppResult.Error   -> _form.update { it.copy(schoolsLoading = false) }
            }
        }
    }

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
                    _form.update { it.copy(locationError = "Could not get current location. Tap on the map to set location.") }
                }
            } catch (e: SecurityException) {
                _form.update { it.copy(locationError = "Location permission required.") }
            } catch (e: Exception) {
                _form.update { it.copy(locationError = "Could not get location: ${e.message}") }
            }
        }
    }

    fun onSchoolSelected(school: School?) {
        _form.update { it.copy(dropoffSchool = school) }
    }

    fun onNotesChange(value: String) {
        _form.update { it.copy(notes = value) }
    }

    fun onRemovePhoto() {
        _form.update { it.copy(photoUri = null, photoUrl = null, isUploadingPhoto = false) }
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

    fun submit() {
        val f = _form.value
        if (f.pickupLat == null || f.pickupLng == null) {
            _form.update { it.copy(locationError = "Please select a pickup location on the map.") }
            return
        }
        viewModelScope.launch {
            _form.update { it.copy(isSubmitting = true, submitError = null) }
            when (val result = createPickupRequest(
                pickupLat       = f.pickupLat,
                pickupLng       = f.pickupLng,
                pickupAddress   = f.pickupAddress,
                dropoffSchoolId = f.dropoffSchool?.id,
                notes           = f.notes.ifBlank { null },
                photoUrl        = f.photoUrl,
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
