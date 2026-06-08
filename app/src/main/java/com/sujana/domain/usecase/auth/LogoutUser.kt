package com.sujana.domain.usecase.auth

import com.sujana.domain.repository.IAuthRepository
import javax.inject.Inject

class LogoutUser @Inject constructor(private val repo: IAuthRepository) {
    suspend operator fun invoke() = repo.logout()
}
