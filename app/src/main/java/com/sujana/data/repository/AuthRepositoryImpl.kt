package com.sujana.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.sujana.core.common.AppError
import com.sujana.core.common.AppResult
import com.sujana.core.network.SujanaApi
import com.sujana.data.local.SessionDataStore
import com.sujana.domain.model.User
import com.sujana.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val api: SujanaApi,
    private val session: SessionDataStore,
) : IAuthRepository {

    override fun currentUserFlow(): Flow<User?> = session.currentUser

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AppResult<User> = runCatching {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val fbUser = result.user!!
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        fbUser.updateProfile(profileUpdate).await()
        fbUser.sendEmailVerification().await()  // user must verify before super-admin bootstrap fires
        fbUser.getIdToken(true).await()  // refresh so backend sees updated displayName claim
        val me = api.getMe()
        val user = me.toDomain()
        session.save(user)
        user
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.toAppError()) },
    )

    override suspend fun login(email: String, password: String): AppResult<User> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val me = api.getMe()
        val user = me.toDomain()
        session.save(user)
        user
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(it.toAppError()) },
    )

    override suspend fun logout() {
        session.clear()
        firebaseAuth.signOut()
    }

    override suspend fun getFreshIdToken(): String? = runCatching {
        firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
    }.getOrNull()
}

private fun com.sujana.shared.dto.MeResponse.toDomain() = User(
    id = id,
    firebaseUid = firebaseUid,
    name = name,
    email = email,
    role = role,
    tenantId = tenantId,
    phone = phone,
)

private fun Throwable.toAppError(): AppError = when (this) {
    is com.google.firebase.auth.FirebaseAuthException -> when (errorCode) {
        "ERROR_USER_NOT_FOUND",
        "ERROR_WRONG_PASSWORD",
        "ERROR_INVALID_CREDENTIAL" -> AppError.Unauthorized(localizedMessage ?: "Invalid credentials")
        else -> AppError.Network(localizedMessage ?: "Auth error", this)
    }
    is retrofit2.HttpException -> AppError.Server(
        code = code().toString(),
        message = message(),
        httpStatus = code(),
    )
    else -> AppError.Network(localizedMessage ?: "Unknown error", this)
}
