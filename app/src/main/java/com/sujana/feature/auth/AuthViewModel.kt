package com.sujana.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sujana.core.common.AppResult
import com.sujana.domain.usecase.auth.LoginUser
import com.sujana.domain.usecase.auth.LogoutUser
import com.sujana.domain.usecase.auth.RegisterUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUser: LoginUser,
    private val registerUser: RegisterUser,
    private val logoutUser: LogoutUser,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    private val _registerForm = MutableStateFlow(RegisterFormState())
    val registerForm: StateFlow<RegisterFormState> = _registerForm.asStateFlow()

    fun onLoginEmailChange(value: String) {
        _loginForm.value = _loginForm.value.copy(email = value, emailError = null)
    }

    fun onLoginPasswordChange(value: String) {
        _loginForm.value = _loginForm.value.copy(password = value, passwordError = null)
    }

    fun onRegisterNameChange(value: String) {
        _registerForm.value = _registerForm.value.copy(name = value, nameError = null)
    }

    fun onRegisterEmailChange(value: String) {
        _registerForm.value = _registerForm.value.copy(email = value, emailError = null)
    }

    fun onRegisterPasswordChange(value: String) {
        _registerForm.value = _registerForm.value.copy(password = value, passwordError = null)
    }

    fun onRegisterConfirmPasswordChange(value: String) {
        _registerForm.value = _registerForm.value.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun login() {
        val form = _loginForm.value
        val emailErr = if (form.email.isBlank()) "Email is required" else if (!form.email.contains('@')) "Enter a valid email" else null
        val passErr = if (form.password.length < 6) "Password must be at least 6 characters" else null
        if (emailErr != null || passErr != null) {
            _loginForm.value = form.copy(emailError = emailErr, passwordError = passErr)
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = loginUser(form.email.trim(), form.password)) {
                is AppResult.Success -> AuthUiState.Success(result.data)
                is AppResult.Error -> AuthUiState.Failure(result.error)
            }
        }
    }

    fun register() {
        val form = _registerForm.value
        val nameErr = if (form.name.isBlank()) "Name is required" else null
        val emailErr = if (form.email.isBlank()) "Email is required" else if (!form.email.contains('@')) "Enter a valid email" else null
        val passErr = if (form.password.length < 6) "Password must be at least 6 characters" else null
        val confirmErr = if (form.confirmPassword != form.password) "Passwords do not match" else null
        if (nameErr != null || emailErr != null || passErr != null || confirmErr != null) {
            _registerForm.value = form.copy(
                nameError = nameErr,
                emailError = emailErr,
                passwordError = passErr,
                confirmPasswordError = confirmErr,
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = registerUser(form.name.trim(), form.email.trim(), form.password)) {
                is AppResult.Success -> AuthUiState.Success(result.data)
                is AppResult.Error -> AuthUiState.Failure(result.error)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUser()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun clearState() {
        _uiState.value = AuthUiState.Idle
    }
}
