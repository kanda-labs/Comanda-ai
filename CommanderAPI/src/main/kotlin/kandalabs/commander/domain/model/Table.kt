package kandalabs.commander.domain.model

import kandalabs.commander.core.extensions.getOrThrow
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for table response data
 */
@Serializable
data class Table(
    val id: Int? = null,
    val number: Int,
    val status: TableStatus,
    val createdAt: LocalDateTime,
    val billId: Int? = null,
    val orders: List<OrderResponse> = emptyList(),
)

enum class TableStatus { OPEN, CLOSED, ON_PAYMENT }

/**
 * Extension function to convert domain Table entity to TableResponse DTO
 */
fun Table.toResponse(): Table {
    return Table(
        id = id.getOrThrow(),
        number = number,
        status = status,
        createdAt = createdAt,
        billId = billId,
        orders = orders // agora inclui as ordens, se presentes
    )
}
