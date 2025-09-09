package co.kandalabs.comandaai.sdk.session

import co.kandalabs.comandaai.core.session.UserRole
import kotlinx.serialization.Serializable

/**
 * Data class representing a user session
 */
@Serializable
data class UserSession(
    val userId: Int,
    val userName: String,
    val name: String,
    val email: String?,
    val token: String,
    val role: UserRole
)