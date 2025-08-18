package kandalabs.commander.domain.model

import co.kandalabs.comandaai.core.enums.UserRole
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for user response data
 */
@Serializable
data class User(
    val id: Int? = null,
    val name: String,
    val userName: String,
    val email: String? = null,
    val active: Boolean = true,
    val role: UserRole = UserRole.WAITER,
    val createdAt: LocalDateTime,
){

}


/**
 * Data Transfer Object for paginated responses
 */
@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * Data Transfer Object for error responses
 */
@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val path: String? = null
)
