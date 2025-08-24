package kandalabs.commander.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Int? = null,
    val billId: Int,
    val tableNumber: Int,
    val userName: String,
    val items: List<ItemOrder>,
    val status: OrderStatus,
    val createdAt: LocalDateTime
)

enum class OrderStatus(val presentationName: String) {
    GRANTED("Atendido"),
    OPEN("Pendente"),
    CANCELED("Cancelado")
}
