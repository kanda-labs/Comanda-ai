package co.kandalabs.comandaai.kitchen.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.domain.model.ItemUnitStatus
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KitchenViewModel(
    private val repository: KitchenRepository,
    private val sessionManager: SessionManager
) : ScreenModel {
    
    private val _state = MutableStateFlow(KitchenScreenState())
    val state: StateFlow<KitchenScreenState> = _state.asStateFlow()
    
    init {
        loadActiveOrders()
        startRealTimeUpdates()
    }
    
    fun loadActiveOrders() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        screenModelScope.launch {
            repository.getActiveOrders()
                .onSuccess { orders ->
                    _state.update { 
                        it.copy(
                            orders = orders,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun updateItemStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ) {
        // Store current state for rollback
        val currentState = _state.value
        
        // OPTIMISTIC UPDATE: Update UI immediately
        val optimisticOrders = updateOrdersOptimistically(
            currentState.orders, orderId, itemId, unitIndex, newStatus
        )
        _state.update { it.copy(orders = optimisticOrders) }
        
        // Make API call
        screenModelScope.launch {
            repository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus)
                .onSuccess {
                    // Success: Keep the optimistic update
                    // No action needed, state is already updated
                }
                .onFailure { error ->
                    // ROLLBACK: Revert to previous state on error
                    _state.update { currentState.copy(error = error.message) }
                }
        }
    }
    
    fun markOrderAsDelivered(orderId: Int) {
        screenModelScope.launch {
            repository.markOrderAsDelivered(orderId)
                .onSuccess {
                    // Remover pedido do estado local em vez de recarregar tudo
                    _state.update { currentState ->
                        val updatedOrders = currentState.orders.filter { it.id != orderId }
                        currentState.copy(orders = updatedOrders)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
        }
    }
    
    fun markItemAsDelivered(orderId: Int, itemId: Int) {
        // Store current state for rollback
        val currentState = _state.value
        
        // OPTIMISTIC UPDATE: Update UI immediately
        val optimisticOrders = markItemAsDeliveredOptimistically(
            currentState.orders, orderId, itemId
        )
        _state.update { it.copy(orders = optimisticOrders) }
        
        // Make API call
        screenModelScope.launch {
            repository.markItemAsDelivered(orderId, itemId)
                .onSuccess {
                    // Success: Keep the optimistic update
                    // No action needed, state is already updated
                }
                .onFailure { error ->
                    // ROLLBACK: Revert to previous state on error
                    _state.update { currentState.copy(error = error.message) }
                }
        }
    }
    
    fun refreshOrders() {
        _state.update { it.copy(isRefreshing = true) }
        
        screenModelScope.launch {
            repository.getActiveOrders()
                .onSuccess { orders ->
                    _state.update { 
                        it.copy(
                            orders = orders,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isRefreshing = false
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    suspend fun getUserSession(): UserSession? {
        return sessionManager.getSession()
    }
    
    fun logout() {
        screenModelScope.launch {
            sessionManager.clearSession()
        }
    }
    
    private fun startRealTimeUpdates() {
        screenModelScope.launch {
            repository.getOrdersRealTime().collect { event ->
                when (event) {
                    is KitchenEvent.Connected -> {
                        _state.update { it.copy(isConnected = true) }
                    }
                    is KitchenEvent.OrdersUpdate -> {
                        _state.update { 
                            it.copy(
                                orders = event.orders,
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                    }
                    is KitchenEvent.Heartbeat -> {
                        // Mantém conexão ativa, nada específico a fazer
                    }
                    is KitchenEvent.Error -> {
                        _state.update { 
                            it.copy(
                                error = event.message,
                                isConnected = false
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Helper functions for optimistic updates
    private fun updateOrdersOptimistically(
        orders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ): List<KitchenOrder> {
        return orders.map { order ->
            if (order.id == orderId) {
                val updatedItems = order.items.map { item ->
                    if (item.itemId == itemId) {
                        val updatedUnitStatuses = item.unitStatuses.mapIndexed { index, unitStatus ->
                            if (index == unitIndex) {
                                unitStatus.copy(status = newStatus)
                            } else {
                                unitStatus
                            }
                        }
                        
                        // Calculate new overall status
                        val newOverallStatus = calculateOverallStatus(updatedUnitStatuses)
                        
                        item.copy(
                            unitStatuses = updatedUnitStatuses,
                            overallStatus = newOverallStatus
                        )
                    } else {
                        item
                    }
                }
                
                // Check if all items are delivered and remove order if so
                val allItemsDelivered = updatedItems.all { item ->
                    item.unitStatuses.all { it.status == ItemStatus.DELIVERED }
                }
                
                if (allItemsDelivered) {
                    null // Will be filtered out
                } else {
                    order.copy(items = updatedItems)
                }
            } else {
                order
            }
        }.filterNotNull()
    }
    
    private fun markItemAsDeliveredOptimistically(
        orders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int
    ): List<KitchenOrder> {
        return orders.map { order ->
            if (order.id == orderId) {
                val updatedItems = order.items.map { item ->
                    if (item.itemId == itemId) {
                        val updatedUnitStatuses = item.unitStatuses.map { unitStatus ->
                            unitStatus.copy(status = ItemStatus.DELIVERED)
                        }
                        item.copy(
                            unitStatuses = updatedUnitStatuses,
                            overallStatus = ItemStatus.DELIVERED
                        )
                    } else {
                        item
                    }
                }
                
                // Check if all items are delivered and remove order if so
                val allItemsDelivered = updatedItems.all { item ->
                    item.unitStatuses.all { it.status == ItemStatus.DELIVERED }
                }
                
                if (allItemsDelivered) {
                    null // Will be filtered out
                } else {
                    order.copy(items = updatedItems)
                }
            } else {
                order
            }
        }.filterNotNull()
    }
    
    private fun calculateOverallStatus(unitStatuses: List<ItemUnitStatus>): ItemStatus {
        return when {
            unitStatuses.all { it.status == ItemStatus.DELIVERED } -> ItemStatus.DELIVERED
            else -> ItemStatus.OPEN
        }
    }
}