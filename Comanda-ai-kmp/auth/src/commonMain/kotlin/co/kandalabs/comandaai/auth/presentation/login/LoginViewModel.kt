package co.kandalabs.comandaai.auth.presentation.login

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.auth.data.LoginRequest
import co.kandalabs.comandaai.auth.domain.AuthRepository
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.core.enums.UserRole
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.session.UserSession
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : StateScreenModel<LoginScreenState>(LoginScreenState()) {

    fun onUsernameChanged(username: String) {
        screenModelScope.launch {
            val error = validateUsername(username)
            mutableState.emit(
                state.value.copy(
                    username = username,
                    usernameError = error
                )
            )
        }
    }

    fun onPasswordChanged(password: String) {
        screenModelScope.launch {
            val error = validatePassword(password)
            mutableState.emit(
                state.value.copy(
                    password = password,
                    passwordError = error
                )
            )
        }
    }

    fun onLogin() {
        screenModelScope.launch {
            if (!state.value.isFormValid) return@launch

            mutableState.emit(state.value.copy(isLoading = true, error = null))

            try {
                val loginRequest = LoginRequest(
                    username = state.value.username,
                    password = state.value.password
                )
                
                val response = authRepository.login(loginRequest)
                
                val session = UserSession(
                    userId = response.id,
                    userName = response.userName,
                    name = response.name,
                    email = response.email,
                    token = response.token,
                    role = try {
                        UserRole.valueOf(response.role)
                    } catch (e: IllegalArgumentException) {
                        println("Invalid role: ${response.role}, defaulting to WAITER")
                        UserRole.WAITER
                    }
                )
                
                sessionManager.saveSession(session)
                
                mutableState.emit(
                    state.value.copy(
                        isLoading = false,
                        error = null
                    )
                )
                
                // Navigate to tables screen
                AuthNavigationCallback.onLoginSuccess()
                
            } catch (e: Exception) {
                mutableState.emit(
                    state.value.copy(
                        isLoading = false,
                        error = co.kandalabs.comandaai.core.error.ComandaAiException.UnknownException(
                            message = e.message ?: "Erro no login"
                        )
                    )
                )
            }
        }
    }

    fun clearError() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(error = null))
        }
    }

    fun togglePasswordVisibility() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(isPasswordVisible = !state.value.isPasswordVisible))
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Usuário é obrigatório"
            username.length < 3 -> "Usuário deve ter pelo menos 3 caracteres"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Senha é obrigatória"
            password.length < 4 -> "Senha deve ter pelo menos 4 caracteres"
            else -> null
        }
    }
}