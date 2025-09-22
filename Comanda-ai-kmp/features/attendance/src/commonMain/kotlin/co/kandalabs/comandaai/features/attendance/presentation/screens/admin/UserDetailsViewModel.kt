package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.features.attendance.domain.models.model.User
import co.kandalabs.comandaai.features.attendance.domain.models.request.UpdateUserRequest
import co.kandalabs.comandaai.features.attendance.domain.usecases.GetUserByIdUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecases.UpdateUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDetailsViewModel(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase
) : ScreenModel {

    private var userId: Int = 0
    private val _uiState = MutableStateFlow(UserDetailsUiState())
    val uiState: StateFlow<UserDetailsUiState> = _uiState.asStateFlow()

    fun setUserId(id: Int) {
        userId = id
    }

    fun loadUser() {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        screenModelScope.launch {
            getUserByIdUseCase(userId)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Erro ao carregar usuário"
                    )
                }
        }
    }

    fun updateUser(
        userName: String,
        fullName: String,
        email: String?,
        isActive: Boolean,
        role: UserRole
    ) {
        if (_uiState.value.isSaving) return

        _uiState.value = _uiState.value.copy(
            isSaving = true,
            error = null,
            successMessage = null
        )

        screenModelScope.launch {
            val request = UpdateUserRequest(
                name = fullName,
                userName = userName,
                email = email?.takeIf { it.isNotBlank() },
                active = isActive,
                role = role
            )

            updateUserUseCase(userId, request)
                .onSuccess { updatedUser ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        user = updatedUser,
                        successMessage = "Usuário atualizado com sucesso!"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = exception.message ?: "Erro ao atualizar usuário"
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

data class UserDetailsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val successMessage: String? = null
)