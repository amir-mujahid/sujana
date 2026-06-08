package com.sujana.domain.repository

import com.sujana.core.common.AppResult
import com.sujana.domain.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    fun currentUserFlow(): Flow<User?>
    suspend fun register(name: String, email: String, password: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun logout()
    suspend fun getFreshIdToken(): String?
}
