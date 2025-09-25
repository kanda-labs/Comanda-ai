package co.kandalabs.comandaai.auth.presentation.login

import co.kandalabs.comandaai.sdk.error.ComandaAiException

internal data class LoginScreenState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: ComandaAiException? = null,
    val isPasswordVisible: Boolean = false
) {
    val isFormValid: Boolean = username.isNotBlank() && password.isNotBlank()
    
    val isContinueButtonEnabled: Boolean = isFormValid && !isLoading
}

internal enum class LoginAction {
    USERNAME_CHANGED,
    PASSWORD_CHANGED,
    LOGIN,
    CLEAR_ERROR,
    TOGGLE_PASSWORD_VISIBILITY
}