package com.sujana.domain.usecase.request

import com.sujana.core.common.AppResult
import com.sujana.domain.model.School
import com.sujana.domain.repository.IRequestRepository
import javax.inject.Inject

class GetSchools @Inject constructor(
    private val repository: IRequestRepository,
) {
    suspend operator fun invoke(): AppResult<List<School>> = repository.getSchools()
}
