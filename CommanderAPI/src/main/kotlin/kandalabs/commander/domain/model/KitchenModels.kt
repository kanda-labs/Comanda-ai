package kandalabs.commander.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class KitchenOrder(
    val id: Int,
    val tableNumber: Int,
    val items: List<KitchenItemDetail>,
    val createdAt: Long
)

@Serializable
data class KitchenItemDetail(
    val itemId: Int,
    val name: String,
    val totalCount: Int,
    val observation: String?,
    val unitStatuses: List<ItemUnitStatus>,
    val overallStatus: ItemStatus
)

@Serializable
data class ItemUnitStatus(
    val unitIndex: Int,
    val status: ItemStatus,
    val updatedAt: Long,
    val updatedBy: String?
)