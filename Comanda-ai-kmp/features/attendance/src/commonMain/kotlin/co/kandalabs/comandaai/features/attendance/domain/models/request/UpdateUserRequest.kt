package co.kandalabs.comandaai.features.attendance.domain.models.request

import co.kandalabs.comandaai.core.session.UserRole
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for updating an existing user
 */
@Serializable
data class UpdateUserRequest(
    val name: String,
    val userName: String,
    val email: String? = null,
    val active: Boolean? = null,
    val role: UserRole? = null
)