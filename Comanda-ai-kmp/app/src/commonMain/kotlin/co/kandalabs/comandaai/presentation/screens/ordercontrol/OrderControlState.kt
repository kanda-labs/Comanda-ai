package co.kandalabs.comandaai.presentation.screens.ordercontrol

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.ItemOrder
import co.kandalabs.comandaai.domain.ItemStatus

data class OrderControlState(
    val order: Order? = null,
    val isLoading: Boolean = false,
    val error: ComandaAiException? = null,
    val userRole: String? = null,
    val showStatusModal: Boolean = false,
    val selectedItem: ItemOrder? = null,
    val expandedItems: Set<String> = emptySet(),
    val selectedIndividualItem: Pair<ItemOrder, Int>? = null, // ItemOrder + index of individual item
    val individualItemStatuses: Map<String, ItemStatus> = emptyMap() // "itemId_index" -> status
)