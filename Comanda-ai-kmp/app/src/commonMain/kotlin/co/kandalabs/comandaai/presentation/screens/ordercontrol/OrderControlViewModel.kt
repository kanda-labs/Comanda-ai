package co.kandalabs.comandaai.presentation.screens.ordercontrol

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.ItemOrder
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.OrderStatus

class OrderControlViewModel(
    private val sessionManager: SessionManager,
    private val orderRepository: OrderRepository
) : ScreenModel {
    
    private val _state = MutableStateFlow(OrderControlState())
    val state: StateFlow<OrderControlState> = _state

    fun setupOrder(order: Order) {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true
            )
            
            try {
                val currentUserSession = sessionManager.getSession()
                
                // Inicializar status individuais baseado no status geral do item
                val individualStatuses = mutableMapOf<String, ItemStatus>()
                order.items.forEach { item ->
                    for (index in 0 until item.count) {
                        val key = "${item.itemId}_$index"
                        individualStatuses[key] = item.status
                    }
                }
                
                _state.value = _state.value.copy(
                    order = order,
                    userRole = currentUserSession?.role?.name,
                    individualItemStatuses = individualStatuses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao carregar dados do usuário"),
                    isLoading = false
                )
            }
        }
    }
    
    fun updateItemStatus(item: ItemOrder, newStatus: ItemStatus) {
        screenModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // TODO: Implementar chamada para API para atualizar status do item
                // Por enquanto, apenas atualizar localmente
                val currentOrder = _state.value.order
                if (currentOrder != null) {
                    val updatedItems = currentOrder.items.map { orderItem ->
                        if (orderItem.itemId == item.itemId) {
                            orderItem.copy(status = newStatus)
                        } else {
                            orderItem
                        }
                    }
                    val updatedOrder = currentOrder.copy(items = updatedItems)
                    
                    // Verificar se o pedido todo está entregue
                    checkAndUpdateOrderStatus(updatedOrder)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao atualizar status"),
                    isLoading = false
                )
            }
        }
    }
    
    fun showStatusModal(item: ItemOrder) {
        _state.value = _state.value.copy(
            showStatusModal = true,
            selectedItem = item
        )
    }
    
    fun hideStatusModal() {
        _state.value = _state.value.copy(
            showStatusModal = false,
            selectedItem = null
        )
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun toggleItemExpansion(itemId: String) {
        val currentExpanded = _state.value.expandedItems
        val newExpanded = if (currentExpanded.contains(itemId)) {
            currentExpanded - itemId
        } else {
            currentExpanded + itemId
        }
        _state.value = _state.value.copy(expandedItems = newExpanded)
    }
    
    fun showIndividualItemStatusModal(item: ItemOrder, individualIndex: Int) {
        _state.value = _state.value.copy(
            showStatusModal = true,
            selectedIndividualItem = Pair(item, individualIndex)
        )
    }
    
    fun updateIndividualItemStatus(item: ItemOrder, individualIndex: Int, newStatus: ItemStatus) {
        screenModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Atualizar status do item individual específico
                val currentStatuses = _state.value.individualItemStatuses.toMutableMap()
                val key = "${item.itemId}_$individualIndex"
                currentStatuses[key] = newStatus
                
                // Verificar se todos os itens individuais deste tipo estão entregues para atualizar o status geral
                val currentOrder = _state.value.order
                if (currentOrder != null) {
                    val updatedItems = currentOrder.items.map { orderItem ->
                        if (orderItem.itemId == item.itemId) {
                            // Verificar o status geral baseado nos status individuais
                            val allIndividualStatuses = (0 until orderItem.count).map { index ->
                                currentStatuses["${orderItem.itemId}_$index"] ?: orderItem.status
                            }
                            
                            val generalStatus = when {
                                allIndividualStatuses.all { it == ItemStatus.DELIVERED } -> ItemStatus.DELIVERED
                                allIndividualStatuses.all { it == ItemStatus.CANCELED } -> ItemStatus.CANCELED
                                allIndividualStatuses.any { it == ItemStatus.DELIVERED } -> ItemStatus.GRANTED
                                else -> allIndividualStatuses.first()
                            }
                            
                            orderItem.copy(status = generalStatus)
                        } else {
                            orderItem
                        }
                    }
                    val updatedOrder = currentOrder.copy(items = updatedItems)
                    
                    _state.value = _state.value.copy(
                        order = updatedOrder,
                        individualItemStatuses = currentStatuses,
                        isLoading = false,
                        showStatusModal = false,
                        selectedIndividualItem = null
                    )
                    
                    // Verificar se o pedido todo está entregue
                    checkAndUpdateOrderStatusFromIndividuals(updatedOrder, currentStatuses)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao atualizar status individual"),
                    isLoading = false
                )
            }
        }
    }
    
    fun deliverAllItems(item: ItemOrder) {
        screenModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
                // Atualizar todos os itens individuais para entregue
                val currentStatuses = _state.value.individualItemStatuses.toMutableMap()
                for (index in 0 until item.count) {
                    val key = "${item.itemId}_$index"
                    currentStatuses[key] = ItemStatus.DELIVERED
                }
                
                val currentOrder = _state.value.order
                if (currentOrder != null) {
                    val updatedItems = currentOrder.items.map { orderItem ->
                        if (orderItem.itemId == item.itemId) {
                            orderItem.copy(status = ItemStatus.DELIVERED)
                        } else {
                            orderItem
                        }
                    }
                    val updatedOrder = currentOrder.copy(items = updatedItems)
                    
                    // Recolher o acordeon após entregar todos os itens
                    val newExpandedItems = _state.value.expandedItems - "${item.itemId}"
                    
                    _state.value = _state.value.copy(
                        order = updatedOrder,
                        individualItemStatuses = currentStatuses,
                        expandedItems = newExpandedItems,
                        isLoading = false
                    )
                    
                    // Verificar se o pedido todo está entregue
                    checkAndUpdateOrderStatusFromIndividuals(updatedOrder, currentStatuses)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao entregar todos os itens"),
                    isLoading = false
                )
            }
        }
    }
    
    private fun checkAndUpdateOrderStatus(updatedOrder: Order, expandedItems: Set<String>? = null) {
        screenModelScope.launch {
            try {
                // Verificar se todos os itens do pedido estão entregues
                val allItemsDelivered = updatedOrder.items.all { it.status == ItemStatus.DELIVERED }
                
                val finalOrder = if (allItemsDelivered) {
                    // Se todos os itens estão entregues, marcar o pedido como atendido
                    val orderToUpdate = updatedOrder.copy(status = OrderStatus.GRANTED)
                    
                    // Fazer chamada para o backend para persistir a mudança
                    try {
                        val result = orderRepository.updateOrder(orderToUpdate.id!!, orderToUpdate)
                        when (result) {
                            is ComandaAiResult.Success -> {
                                result.data
                            }
                            is ComandaAiResult.Failure -> {
                                println("Erro ao atualizar status do pedido no backend: ${result.exception.message}")
                                orderToUpdate
                            }
                        }
                    } catch (e: Exception) {
                        // Em caso de erro na API, manter a atualização local
                        println("Erro ao atualizar status do pedido no backend: ${e.message}")
                        orderToUpdate
                    }
                } else {
                    updatedOrder
                }
                
                _state.value = _state.value.copy(
                    order = finalOrder,
                    isLoading = false,
                    showStatusModal = false,
                    selectedIndividualItem = null,
                    expandedItems = expandedItems ?: _state.value.expandedItems
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao verificar status do pedido"),
                    isLoading = false
                )
            }
        }
    }
    
    private fun checkAndUpdateOrderStatusFromIndividuals(updatedOrder: Order, individualStatuses: Map<String, ItemStatus>) {
        screenModelScope.launch {
            try {
                // Verificar se todos os itens individuais estão entregues
                val allIndividualsDelivered = updatedOrder.items.all { item ->
                    (0 until item.count).all { index ->
                        val key = "${item.itemId}_$index"
                        individualStatuses[key] == ItemStatus.DELIVERED
                    }
                }
                
                val finalOrder = if (allIndividualsDelivered) {
                    // Se todos os itens individuais estão entregues, marcar o pedido como atendido
                    val orderToUpdate = updatedOrder.copy(status = OrderStatus.GRANTED)
                    
                    // Fazer chamada para o backend para persistir a mudança
                    try {
                        val result = orderRepository.updateOrder(orderToUpdate.id!!, orderToUpdate)
                        when (result) {
                            is ComandaAiResult.Success -> {
                                result.data
                            }
                            is ComandaAiResult.Failure -> {
                                println("Erro ao atualizar status do pedido no backend: ${result.exception.message}")
                                orderToUpdate
                            }
                        }
                    } catch (e: Exception) {
                        // Em caso de erro na API, manter a atualização local
                        // mas você pode decidir tratar isso de forma diferente
                        println("Erro ao atualizar status do pedido no backend: ${e.message}")
                        orderToUpdate
                    }
                } else {
                    updatedOrder
                }
                
                _state.value = _state.value.copy(
                    order = finalOrder
                )
            } catch (e: Exception) {
                // Tratar erro geral
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao verificar status do pedido")
                )
            }
        }
    }
}