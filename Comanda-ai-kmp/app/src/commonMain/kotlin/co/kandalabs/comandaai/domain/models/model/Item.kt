package kandalabs.commander.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int?,
    val name: String,
    val value: Int,
    val category: ItemCategory,
    val description: String? = null,
)

@Serializable
data class ItemOrder(
    val itemId: Int,
    val orderId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null,
    val status: ItemStatus
)

enum class ItemStatus { 
    GRANTED, 
    OPEN, 
    DELIVERED, 
    CANCELED 
}

enum class ItemCategory { SKEWER, DRINK, SNACK, PROMOTIONAL }
