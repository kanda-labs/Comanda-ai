package co.kandalabs.comandaai.auth.presentation.login

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch

internal class LoginViewModel : StateScreenModel<LoginScreenState>(LoginScreenState()) {

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

            // Simulação de login (aqui seria a integração com API real)
            try {
                // Simulate network delay
                kotlinx.coroutines.delay(1500)
                
                // For demo purposes, accept any non-empty credentials
                if (state.value.username.isNotBlank() && state.value.password.isNotBlank()) {
                    // Login successful - aqui você navegaria para a tela principal
                    mutableState.emit(
                        state.value.copy(
                            isLoading = false,
                            error = null
                        )
                    )
                    // TODO: Navigate to main screen when integrated
                } else {
                    throw Exception("Credenciais inválidas")
                }
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