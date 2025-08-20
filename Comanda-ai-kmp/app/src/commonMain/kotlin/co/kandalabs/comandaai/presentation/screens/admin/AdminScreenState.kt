package co.kandalabs.comandaai.presentation.screens.admin

import co.kandalabs.comandaai.core.session.UserSession

/**
 * UI state for the Admin screen
 */
data class AdminScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userSession: UserSession? = null,
    val hasAdminAccess: Boolean = false,
    val title: String = "Administração"
)