package kandalabs.commander.presentation.models.response

import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO that matches the frontend Table model exactly
 */
@Serializable
data class TableDto(
    val id: Int? = null,
    val number: Int,
    val status: TableStatusDto,
    val billId: Int? = null,
    val orders: List<OrderDto> = emptyList(),
)

@Serializable
enum class TableStatusDto {
    @SerialName("OPEN")
    OCCUPIED,
    @SerialName("CLOSED") 
    FREE,
    ON_PAYMENT
}

@Serializable
data class OrderDto(
    val id: Int
)

/**
 * Extension function to convert domain Table to frontend-compatible TableDto
 */
fun Table.toDto(): TableDto {
    return TableDto(
        id = id,
        number = number,
        status = when(status) {
            TableStatus.OPEN -> TableStatusDto.OCCUPIED
            TableStatus.CLOSED -> TableStatusDto.FREE
            TableStatus.ON_PAYMENT -> TableStatusDto.ON_PAYMENT
        },
        billId = billId,
        orders = emptyList() // Simplify for migration response
    )
}