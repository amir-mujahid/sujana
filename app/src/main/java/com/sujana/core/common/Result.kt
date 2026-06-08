package com.sujana.core.common

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
}

sealed class AppError {
    data class Network(val message: String, val cause: Throwable? = null) : AppError()
    data class Server(val code: String, val message: String, val httpStatus: Int) : AppError()
    data class Unauthorized(val message: String = "Session expired") : AppError()
    data class NotFound(val resource: String) : AppError()
    data class Unknown(val message: String, val cause: Throwable? = null) : AppError()
}
