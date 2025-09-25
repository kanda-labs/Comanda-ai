package co.kandalabs.comandaai.auth.presentation.login

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.network.NetworkConfig
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.sdk.auth.AuthManager
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.UserSession
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val sessionManager: SessionManager
) : StateScreenModel<LoginScreenState>(LoginScreenState()) {

    fun onUsernameChanged(username: String) {
        screenModelScope.launch {
            mutableState.emit(
                state.value.copy(
                    username = username,
                    usernameError = null, // Clear error when typing
                    error = null // Clear general error when user starts typing
                )
            )
        }
    }

    fun onPasswordChanged(password: String) {
        screenModelScope.launch {
            mutableState.emit(
                state.value.copy(
                    password = password,
                    passwordError = null, // Clear error when typing
                    error = null // Clear general error when user starts typing
                )
            )
        }
    }

    fun onLogin() {
        screenModelScope.launch {
            // Normalize text inputs before validation and login
            val normalizedUsername = normalizeText(state.value.username)
            val normalizedPassword = normalizeText(state.value.password)

            // Update state with normalized values
            mutableState.emit(
                state.value.copy(
                    username = normalizedUsername,
                    password = normalizedPassword
                )
            )

            // Validate fields before attempting login
            val usernameError = validateUsername(normalizedUsername)
            val passwordError = validatePassword(normalizedPassword)

            if (usernameError != null || passwordError != null) {
                mutableState.emit(
                    state.value.copy(
                        username = normalizedUsername,
                        password = normalizedPassword,
                        usernameError = usernameError,
                        passwordError = passwordError
                    )
                )
                return@launch
            }

            mutableState.emit(
                state.value.copy(
                    username = normalizedUsername,
                    password = normalizedPassword,
                    isLoading = true,
                    error = null
                )
            )

            try {
                // Use AuthManager to prevent "Parent job is Completed" errors
                val response = AuthManager.performLogin(
                    username = normalizedUsername,
                    password = normalizedPassword,
                    baseUrl = NetworkConfig.currentBaseUrl.removeSuffix("/") + "/api/v1"
                )
                
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
                val errorMessage = when {
                    e.message?.contains("401") == true ||
                    e.message?.contains("unauthorized") == true ||
                    e.message?.contains("Unauthorized") == true -> "Usuário ou senha incorretos. Verifique suas credenciais e tente novamente."

                    e.message?.contains("network") == true ||
                    e.message?.contains("timeout") == true ||
                    e.message?.contains("connection") == true -> "Problemas de conexão. Verifique sua internet e tente novamente."

                    e.message?.contains("server") == true ||
                    e.message?.contains("500") == true -> "Servidor temporariamente indisponível. Tente novamente em alguns instantes."

                    else -> "Não foi possível fazer login no momento. Tente novamente."
                }

                mutableState.emit(
                    state.value.copy(
                        isLoading = false,
                        error = co.kandalabs.comandaai.sdk.error.ComandaAiException.UnknownException(
                            message = errorMessage
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

    fun validateUsernameField() {
        screenModelScope.launch {
            val error = validateUsername(state.value.username)
            mutableState.emit(
                state.value.copy(usernameError = error)
            )
        }
    }

    fun validatePasswordField() {
        screenModelScope.launch {
            val error = validatePassword(state.value.password)
            mutableState.emit(
                state.value.copy(passwordError = error)
            )
        }
    }

    private fun validateUsername(username: String): String? {
        return when {
            username.isBlank() -> "Digite seu usuário para continuar"
            username.length < 3 -> "O usuário deve conter pelo menos 3 caracteres"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Digite sua senha para continuar"
            password.length < 4 -> "A senha deve conter pelo menos 4 caracteres"
            else -> null
        }
    }

    private fun normalizeText(text: String): String {
        return text.trim().lowercase()
    }
}