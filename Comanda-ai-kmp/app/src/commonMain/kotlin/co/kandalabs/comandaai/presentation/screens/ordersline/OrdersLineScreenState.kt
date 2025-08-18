package co.kandalabs.comandaai.presentation.screens.ordersline

import co.kandalabs.comandaai.core.error.ComandaAiException
import kandalabs.commander.domain.model.Order
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class OrdersLineScreenState(
    val orders: ImmutableList<Order> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: ComandaAiException? = null,
    val isRefreshing: Boolean = false,
    val title: String = "Fila de Pedidos",
    val selectedOrderId: Int? = null
) {
    val pendingOrders: List<Order>
        get() = orders.filter { it.status == kandalabs.commander.domain.model.OrderStatus.OPEN }
            .sortedBy { it.createdAt }
    
    val completedOrders: List<Order>
        get() = orders.filter { it.status == kandalabs.commander.domain.model.OrderStatus.GRANTED }
            .sortedByDescending { it.createdAt }
            .take(10) // Show only last 10 completed orders
    
    val hasOrders: Boolean
        get() = orders.isNotEmpty()
}
