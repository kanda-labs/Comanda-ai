package co.kandalabs.comandaai.features.attendance.domain.models.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class Bill(
    val id: Int? = null,
    val tableId: Int?,
    val tableNumber: Int?,
    val orders: List<Order>,
    val status: BillStatus,
    val createdAt: LocalDateTime
)

enum class BillStatus { PAID, SCAM, OPEN, CANCELED }
