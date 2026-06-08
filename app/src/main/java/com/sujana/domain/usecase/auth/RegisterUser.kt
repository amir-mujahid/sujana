package com.sujana.domain.usecase.auth

import com.sujana.core.common.AppResult
import com.sujana.domain.model.User
import com.sujana.domain.repository.IAuthRepository
import javax.inject.Inject

class RegisterUser @Inject constructor(private val repo: IAuthRepository) {
    suspend operator fun invoke(name: String, email: String, password: String): AppResult<User> =
        repo.register(name, email, password)
}
