package kandalabs.commander.presentation.models.request

import kandalabs.commander.domain.model.ItemCategory
import kotlinx.serialization.Serializable

@Serializable
data class CreateItemRequest(
    val name: String,
    val value: Int,
    val category: ItemCategory,
    val description: String? = null
)

@Serializable
data class UpdateItemRequest(
    val status: String
)
