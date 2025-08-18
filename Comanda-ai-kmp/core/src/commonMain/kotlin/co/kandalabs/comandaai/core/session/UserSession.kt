package co.kandalabs.comandaai.core.session

import co.kandalabs.comandaai.core.enums.UserRole
import kotlinx.serialization.Serializable

/**
 * Data class representing a user session
 */
@Serializable
data class UserSession(
    val userId: Int,
    val userName: String,
    val email: String?,
    val token: String,
    val role: UserRole
)