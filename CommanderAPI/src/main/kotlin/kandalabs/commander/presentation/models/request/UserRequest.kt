package kandalabs.commander.presentation.models.request

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for creating a new user
 */
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String? = null,
    val active: Boolean = true
)

/**
 * Data Transfer Object for updating an existing user
 */
@Serializable
data class UpdateUserRequest(
    val name: String,
    val email: String? = null,
    val active: Boolean? = null
)

/**
 * Data Transfer Object for pagination parameters
 */
@Serializable
data class PaginationRequest(
    val page: Int = 1,
    val size: Int = 10
)

