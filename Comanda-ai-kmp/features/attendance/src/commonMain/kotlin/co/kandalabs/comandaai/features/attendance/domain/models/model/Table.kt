package co.kandalabs.comandaai.features.attendance.domain.models.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for table response data
 */
@Serializable
data class Table(
    val id: Int? = null,
    val number: Int,
    val status: TableStatus,
    val billId: Int? = null,
    val orders: List<Order> = emptyList(),
)

enum class TableStatus(val presentationName: String) {
    @SerialName("OPEN")
    OCCUPIED("Ocupada"),
    @SerialName("CLOSED")
    FREE("Livre"),
    ON_PAYMENT("Em pagamento")
}
