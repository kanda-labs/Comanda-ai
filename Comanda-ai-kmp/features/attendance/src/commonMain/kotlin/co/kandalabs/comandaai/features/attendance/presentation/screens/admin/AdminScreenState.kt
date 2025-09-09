package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import co.kandalabs.comandaai.sdk.session.UserSession

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