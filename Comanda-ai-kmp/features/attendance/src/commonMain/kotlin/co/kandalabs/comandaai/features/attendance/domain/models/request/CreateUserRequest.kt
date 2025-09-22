package co.kandalabs.comandaai.features.attendance.domain.models.request

import co.kandalabs.comandaai.core.session.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val name: String,
    val userName: String,
    val email: String? = null,
    val password: String,
    val active: Boolean = true,
    val role: UserRole = UserRole.WAITER
)