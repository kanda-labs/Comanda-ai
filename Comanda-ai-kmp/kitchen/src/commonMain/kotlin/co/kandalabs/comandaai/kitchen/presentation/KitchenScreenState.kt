package co.kandalabs.comandaai.kitchen.presentation

import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.domain.ItemStatus

enum class OrderFilter {
    ACTIVE,
    DELIVERED
}

data class KitchenScreenState(
    val activeOrders: List<KitchenOrder> = emptyList(),
    val deliveredOrders: List<KitchenOrder> = emptyList(),
    val currentFilter: OrderFilter = OrderFilter.ACTIVE,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false
) {
    val orders: List<KitchenOrder>
        get() = when (currentFilter) {
            OrderFilter.ACTIVE -> activeOrders
            OrderFilter.DELIVERED -> deliveredOrders.sortedByDescending { order ->
                // Get the most recent delivery timestamp from all delivered items in the order
                order.items
                    .flatMap { item -> item.unitStatuses }
                    .filter { unit -> unit.status == ItemStatus.DELIVERED }
                    .maxOfOrNull { unit -> unit.updatedAt } ?: order.createdAt
            }
        }
}