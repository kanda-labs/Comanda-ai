package kandalabs.commander.presentation.models.response

import kandalabs.commander.domain.model.TableStatus
import kotlinx.serialization.Serializable

/**
 * Lightweight table response for listing screens that only need basic table info
 */
@Serializable
data class HomeTableResponse(
    val id: Int,
    val number: Int,
    val status: TableStatus
)