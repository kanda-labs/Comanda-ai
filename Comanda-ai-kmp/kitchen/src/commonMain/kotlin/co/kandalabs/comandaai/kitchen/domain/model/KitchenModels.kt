package co.kandalabs.comandaai.kitchen.domain.model

import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus
import kotlinx.serialization.Serializable

@Serializable
data class KitchenOrder(
    val id: Int,
    val tableNumber: Int,
    val userName: String,
    val items: List<KitchenItemDetail>,
    val createdAt: Long,
    val updatedAt: Long? = null
)

@Serializable
data class KitchenItemDetail(
    val itemId: Int,
    val name: String,
    val totalCount: Int,
    val observation: String?,
    val unitStatuses: List<ItemUnitStatus>,
    val overallStatus: ItemStatus,
    val category: ItemCategory
)

@Serializable
data class ItemUnitStatus(
    val unitIndex: Int,
    val status: ItemStatus,
    val updatedAt: Long,
    val updatedBy: String?
)