package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.usecases.CreateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersManagementViewModel(
    private val createUserUseCase: CreateUserUseCase
) : ScreenModel {

    private val _uiState = MutableStateFlow(UsersManagementUiState())
    val uiState: StateFlow<UsersManagementUiState> = _uiState.asStateFlow()

    fun createUser(
        userName: String,
        fullName: String,
        email: String?,
        password: String,
        role: UserRole
    ) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null,
            successMessage = null
        )

        screenModelScope.launch {
            val request = CreateUserRequest(
                name = fullName,
                userName = userName,
                email = email?.takeIf { it.isNotBlank() },
                password = password,
                role = role
            )

            createUserUseCase(request)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Usuário '${user.name}' criado com sucesso!",
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erro ao criar usuário",
                        successMessage = null
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}

data class UsersManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)