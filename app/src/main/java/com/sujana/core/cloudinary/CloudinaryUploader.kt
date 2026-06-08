package com.sujana.core.cloudinary

import android.content.Context
import android.net.Uri
import com.sujana.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CloudinaryUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("cloudinary") private val httpClient: OkHttpClient,
) {
    suspend fun uploadImage(uri: Uri, folder: String): String = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Cannot read image from URI")

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/*".toMediaType()))
            .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .addFormDataPart("folder", folder)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
            .post(body)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: error("Empty Cloudinary response")
        if (!response.isSuccessful) error("Cloudinary upload failed (${response.code}): $responseBody")

        JSONObject(responseBody).getString("secure_url")
    }
}
