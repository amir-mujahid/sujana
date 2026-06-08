package com.sujana.domain.usecase.auth

import com.sujana.domain.model.User
import com.sujana.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCurrentSession @Inject constructor(private val repo: IAuthRepository) {
    operator fun invoke(): Flow<User?> = repo.currentUserFlow()
}
