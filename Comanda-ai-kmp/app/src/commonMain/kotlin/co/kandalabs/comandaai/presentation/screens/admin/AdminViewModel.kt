package co.kandalabs.comandaai.presentation.screens.admin

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.LogoutManager
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.enums.UserRole
import kotlinx.coroutines.launch

internal class AdminViewModel(
    private val sessionManager: SessionManager
) : StateScreenModel<AdminScreenState>(AdminScreenState()) {

    init {
        loadAdminData()
    }

    private fun loadAdminData() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(isLoading = true, error = null))
            
            try {
                val userSession = sessionManager.getSession()
                val hasAdminAccess = userSession?.role in listOf(UserRole.ADMIN, UserRole.MANAGER)
                
                mutableState.emit(
                    state.value.copy(
                        isLoading = false,
                        userSession = userSession,
                        hasAdminAccess = hasAdminAccess,
                        error = if (!hasAdminAccess) "Acesso negado. Apenas administradores e gerentes podem acessar esta área." else null
                    )
                )
            } catch (exception: Exception) {
                mutableState.emit(
                    state.value.copy(
                        isLoading = false,
                        error = "Erro ao carregar dados do administrador: ${exception.message}"
                    )
                )
            }
        }
    }

    fun handleAction(action: AdminAction) {
        when (action) {
            AdminAction.Retry -> loadAdminData()
            else -> {
                // Navigation actions will be handled by the screen
            }
        }
    }

    fun logout() {
        // Use LogoutManager to prevent "Parent job is Completed" errors
        LogoutManager.performLogout(sessionManager)
    }

    suspend fun getUserSession() = sessionManager.getSession()
}