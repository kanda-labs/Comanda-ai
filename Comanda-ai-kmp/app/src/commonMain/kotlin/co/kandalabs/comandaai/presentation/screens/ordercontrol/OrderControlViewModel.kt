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
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.ItemOrder
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.domain.models.model.OrderStatus

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
    
    fun setupOrderById(orderId: Int) {
        screenModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val currentUserSession = sessionManager.getSession()
                
                when (val result = orderRepository.getOrderByIdWithStatuses(orderId)) {
                    is ComandaAiResult.Success -> {
                        val orderWithStatuses = result.data
                        
                        // Converter para Order regular para compatibilidade
                        val order = Order(
                            id = orderWithStatuses.id,
                            billId = orderWithStatuses.billId,
                            tableNumber = orderWithStatuses.tableNumber,
                            userName = orderWithStatuses.userName,
                            items = orderWithStatuses.items,
                            status = orderWithStatuses.status,
                            createdAt = orderWithStatuses.createdAt
                        )
                        
                        _state.value = _state.value.copy(
                            order = order,
                            userRole = currentUserSession?.role?.name,
                            individualItemStatuses = orderWithStatuses.individualStatuses,
                            isLoading = false
                        )
                    }
                    is ComandaAiResult.Failure -> {
                        _state.value = _state.value.copy(
                            error = result.exception,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = ComandaAiException.UnknownException(e.message ?: "Erro ao carregar pedido"),
                    isLoading = false
                )
            }
        }
    }
    
    fun updateItemStatus(item: ItemOrder, newStatus: ItemStatus) {
        screenModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                
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
                    
                    // Atualizar os status individuais também
                    val currentStatuses = _state.value.individualItemStatuses.toMutableMap()
                    for (index in 0 until item.count) {
                        val key = "${item.itemId}_$index"
                        currentStatuses[key] = newStatus
                    }
                    
                    // Persistir as mudanças no backend
                    try {
                        val currentUserSession = sessionManager.getSession()
                        val updatedBy = currentUserSession?.userName ?: "system"
                        
                        val result = orderRepository.updateOrderWithIndividualStatuses(
                            updatedOrder.id!!,
                            updatedOrder,
                            currentStatuses,
                            updatedBy
                        )
                        
                        when (result) {
                            is ComandaAiResult.Success -> {
                                _state.value = _state.value.copy(
                                    order = result.data,
                                    individualItemStatuses = currentStatuses,
                                    isLoading = false,
                                    showStatusModal = false,
                                    selectedItem = null
                                )
                            }
                            is ComandaAiResult.Failure -> {
                                println("Erro ao persistir status no backend: ${result.exception.message}")
                                // Manter atualização local mesmo com erro no backend
                                _state.value = _state.value.copy(
                                    order = updatedOrder,
                                    individualItemStatuses = currentStatuses,
                                    isLoading = false,
                                    showStatusModal = false,
                                    selectedItem = null
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Erro ao chamar API para persistir status: ${e.message}")
                        // Manter atualização local mesmo com erro na API
                        _state.value = _state.value.copy(
                            order = updatedOrder,
                            individualItemStatuses = currentStatuses,
                            isLoading = false,
                            showStatusModal = false,
                            selectedItem = null
                        )
                    }
                    
                    // Verificar se o pedido todo está entregue
                    checkAndUpdateOrderStatusFromIndividuals(updatedOrder, currentStatuses)
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
                
                // Garantir que todos os outros itens individuais deste tipo mantêm seus status no mapa
                for (index in 0 until item.count) {
                    val otherKey = "${item.itemId}_$index"
                    if (!currentStatuses.containsKey(otherKey)) {
                        currentStatuses[otherKey] = item.status
                    }
                }
                
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
                                allIndividualStatuses.any { it == ItemStatus.DELIVERED } -> ItemStatus.DELIVERED
                                else -> ItemStatus.PENDING
                            }
                            
                            orderItem.copy(status = generalStatus)
                        } else {
                            orderItem
                        }
                    }
                    val updatedOrder = currentOrder.copy(items = updatedItems)
                    
                    // Persistir as mudanças no backend
                    try {
                        val currentUserSession = sessionManager.getSession()
                        val updatedBy = currentUserSession?.userName ?: "system"
                        
                        val result = orderRepository.updateOrderWithIndividualStatuses(
                            updatedOrder.id!!,
                            updatedOrder,
                            currentStatuses,
                            updatedBy
                        )
                        
                        when (result) {
                            is ComandaAiResult.Success -> {
                                // Atualizar com o resultado do backend
                                _state.value = _state.value.copy(
                                    order = result.data,
                                    individualItemStatuses = currentStatuses,
                                    isLoading = false,
                                    showStatusModal = false,
                                    selectedIndividualItem = null
                                )
                            }
                            is ComandaAiResult.Failure -> {
                                println("Erro ao persistir status individual no backend: ${result.exception.message}")
                                // Manter atualização local mesmo com erro no backend
                                _state.value = _state.value.copy(
                                    order = updatedOrder,
                                    individualItemStatuses = currentStatuses,
                                    isLoading = false,
                                    showStatusModal = false,
                                    selectedIndividualItem = null
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Erro ao chamar API para persistir status individual: ${e.message}")
                        // Manter atualização local mesmo com erro na API
                        _state.value = _state.value.copy(
                            order = updatedOrder,
                            individualItemStatuses = currentStatuses,
                            isLoading = false,
                            showStatusModal = false,
                            selectedIndividualItem = null
                        )
                    }
                    
                    // Verificar se o pedido todo está entregue apenas se não houver erro
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
                    
                    // Persistir as mudanças no backend
                    try {
                        val currentUserSession = sessionManager.getSession()
                        val updatedBy = currentUserSession?.userName ?: "system"
                        
                        val result = orderRepository.updateOrderWithIndividualStatuses(
                            updatedOrder.id!!,
                            updatedOrder,
                            currentStatuses,
                            updatedBy
                        )
                        
                        when (result) {
                            is ComandaAiResult.Success -> {
                                // Recolher o acordeon após entregar todos os itens
                                val newExpandedItems = _state.value.expandedItems - "${item.itemId}"
                                
                                _state.value = _state.value.copy(
                                    order = result.data,
                                    individualItemStatuses = currentStatuses,
                                    expandedItems = newExpandedItems,
                                    isLoading = false
                                )
                            }
                            is ComandaAiResult.Failure -> {
                                println("Erro ao persistir entrega de todos os itens no backend: ${result.exception.message}")
                                // Recolher o acordeon mesmo com erro no backend
                                val newExpandedItems = _state.value.expandedItems - "${item.itemId}"
                                
                                _state.value = _state.value.copy(
                                    order = updatedOrder,
                                    individualItemStatuses = currentStatuses,
                                    expandedItems = newExpandedItems,
                                    isLoading = false
                                )
                            }
                        }
                    } catch (e: Exception) {
                        println("Erro ao chamar API para persistir entrega de todos os itens: ${e.message}")
                        // Recolher o acordeon mesmo com erro na API
                        val newExpandedItems = _state.value.expandedItems - "${item.itemId}"
                        
                        _state.value = _state.value.copy(
                            order = updatedOrder,
                            individualItemStatuses = currentStatuses,
                            expandedItems = newExpandedItems,
                            isLoading = false
                        )
                    }
                    
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
                    val orderToUpdate = updatedOrder.copy(status = OrderStatus.DELIVERED)
                    
                    // Fazer chamada para o backend para persistir a mudança
                    try {
                        val currentUserSession = sessionManager.getSession()
                        val updatedBy = currentUserSession?.userName ?: "system"
                        
                        val result = orderRepository.updateOrderWithIndividualStatuses(
                            orderToUpdate.id!!,
                            orderToUpdate,
                            _state.value.individualItemStatuses,
                            updatedBy
                        )
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
                    val orderToUpdate = updatedOrder.copy(status = OrderStatus.DELIVERED)
                    
                    // Fazer chamada para o backend para persistir a mudança
                    try {
                        val currentUserSession = sessionManager.getSession()
                        val updatedBy = currentUserSession?.userName ?: "system"
                        
                        val result = orderRepository.updateOrderWithIndividualStatuses(
                            orderToUpdate.id!!,
                            orderToUpdate,
                            individualStatuses,
                            updatedBy
                        )
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