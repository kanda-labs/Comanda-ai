package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.enums.UserRole
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for creating a new user
 */
@Serializable
data class CreateUserRequest(
    val name: String,
    val userName: String,
    val email: String? = null,
    val password: String,
    val active: Boolean = true,
    val role: UserRole = UserRole.WAITER
)

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

/**
 * Data Transfer Object for pagination parameters
 */
@Serializable
data class PaginationRequest(
    val page: Int = 1,
    val size: Int = 10
)

