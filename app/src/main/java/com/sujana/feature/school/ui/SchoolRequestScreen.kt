package com.sujana.feature.school.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.sujana.core.theme.Radii
import com.sujana.core.theme.Spacing
import com.sujana.feature.request.ui.LocationPickerMap
import com.sujana.feature.school.SchoolRequestViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolRequestScreen(
    onNavigateUp: () -> Unit,
    onSubmitSuccess: (requestId: String) -> Unit,
    viewModel: SchoolRequestViewModel,
) {
    val form by viewModel.form.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.submitSuccess.collect { id -> onSubmitSuccess(id) }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onUseCurrentLocation()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.onPhotoSelected(uri) }

    if (showDatePicker) {
        val today = LocalDate.now()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (form.scheduledDate ?: today)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        viewModel.onScheduledDateSelected(date)
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    if (showTimePicker) {
        val existingTime = form.scheduledTime ?: LocalTime.of(9, 0)
        val timePickerState = rememberTimePickerState(
            initialHour   = existingTime.hour,
            initialMinute = existingTime.minute,
            is24Hour      = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select pickup time") },
            text  = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    viewModel.onScheduledTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request School Pickup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LocationPickerMap(
                selectedLat          = form.pickupLat,
                selectedLng          = form.pickupLng,
                onLocationSelected   = viewModel::onMapTap,
                onUseCurrentLocation = {
                    val fineGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (fineGranted) {
                        viewModel.onUseCurrentLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
            ) {
                Spacer(Modifier.height(Spacing.lg))

                SchoolFormLabel("Pickup Location")
                Spacer(Modifier.height(Spacing.sm))
                if (form.pickupAddress.isNotBlank()) {
                    Text(
                        text  = form.pickupAddress,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                } else {
                    Text(
                        text  = "Tap on the map or use current location",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (form.locationError != null) {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text  = form.locationError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(Modifier.height(Spacing.xl))

                SchoolFormLabel("Scheduled Date")
                Spacer(Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Radii.chip))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radii.chip))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text  = form.scheduledDate?.toString() ?: "Select date (optional)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (form.scheduledDate != null) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (form.scheduledDate != null) {
                    Spacer(Modifier.height(Spacing.md))
                    SchoolFormLabel("Preferred Time")
                    Spacer(Modifier.height(Spacing.sm))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radii.chip))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radii.chip))
                            .clickable { showTimePicker = true }
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        verticalAlignment   = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text  = form.scheduledTime?.let { t ->
                                "%02d:%02d".format(t.hour, t.minute)
                            } ?: "Select time (optional)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (form.scheduledTime != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.xl))

                SchoolFormLabel("Notes (optional)")
                Spacer(Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value         = form.notes,
                    onValueChange = viewModel::onNotesChange,
                    modifier      = Modifier.fillMaxWidth(),
                    placeholder   = { Text("Type of waste, quantity, access instructions…") },
                    maxLines      = 4,
                    shape         = RoundedCornerShape(Radii.chip),
                )

                Spacer(Modifier.height(Spacing.xl))

                SchoolFormLabel("Photo (optional)")
                Spacer(Modifier.height(Spacing.sm))
                SchoolPhotoPicker(
                    photoUri      = form.photoUri,
                    photoUrl      = form.photoUrl,
                    isUploading   = form.isUploadingPhoto,
                    onPickPhoto   = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemovePhoto = viewModel::onRemovePhoto,
                )

                Spacer(Modifier.height(Spacing.xl))

                if (form.submitError != null) {
                    Text(
                        text  = form.submitError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(Spacing.sm))
                }

                Button(
                    onClick  = viewModel::submit,
                    enabled  = !form.isSubmitting && !form.isUploadingPhoto && form.pickupLat != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (form.isSubmitting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Submit Pickup Request")
                    }
                }

                Spacer(Modifier.height(Spacing.xxxl))
            }
        }
    }
}

@Composable
private fun SchoolFormLabel(label: String) {
    Text(
        text  = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SchoolPhotoPicker(
    photoUri: android.net.Uri?,
    photoUrl: String?,
    isUploading: Boolean,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    if (photoUri == null) {
        TextButton(
            onClick  = onPickPhoto,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radii.chip))
                .clip(RoundedCornerShape(Radii.chip)),
        ) {
            Icon(Icons.Filled.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.size(Spacing.sm))
            Text("Add photo", color = MaterialTheme.colorScheme.secondary)
        }
    } else {
        Box(modifier = Modifier.size(120.dp)) {
            AsyncImage(
                model              = photoUri,
                contentDescription = "Selected photo",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(RoundedCornerShape(Radii.card)),
            )
            if (isUploading) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(Radii.card))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                }
            } else if (photoUrl != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Uploaded",
                        tint     = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
            IconButton(
                onClick  = onRemovePhoto,
                modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(24.dp),
            ) {
                Icon(Icons.Filled.Close, "Remove photo", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            }
        }
    }
}
