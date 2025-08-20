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
import kotlinx.coroutines.Job

/**
 * ViewModel for the Kitchen screen that manages kitchen orders and item statuses.
 * 
 * Features:
 * - Real-time order updates via SSE
 * - Optimistic updates for better UX
 * - Individual item unit status management
 * - Automatic order removal when all items are delivered
 */
class KitchenViewModel(
    private val repository: KitchenRepository,
    private val sessionManager: SessionManager
) : ScreenModel {
    
    private val _state = MutableStateFlow(KitchenScreenState())
    val state: StateFlow<KitchenScreenState> = _state.asStateFlow()
    
    private var sseJob: Job? = null
    
    init {
        loadActiveOrders()
        startRealTimeUpdates()
    }
    
    // =================================
    // Public API Methods
    // =================================
    
    /**
     * Load active orders from the repository.
     * Called manually to refresh the orders list.
     */
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
    
    /**
     * Update the status of a specific unit of an item in an order.
     * Uses optimistic updates for better UX.
     */
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
    
    /**
     * Mark an entire order as delivered.
     * Removes the order from the active orders list.
     */
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
    
    /**
     * Mark all units of a specific item as delivered.
     * Uses optimistic updates for better UX.
     */
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
    
    /**
     * Refresh orders with a loading indicator.
     * Used for pull-to-refresh functionality.
     * If disconnected, also attempts to reconnect SSE.
     */
    fun refreshOrders() {
        _state.update { it.copy(isRefreshing = true) }
        
        screenModelScope.launch {
            // If not connected, restart SSE connection
            if (!_state.value.isConnected) {
                reconnectSSE()
            }
            
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
    
    /**
     * Reconnect the SSE connection.
     * Cancels existing connection and starts a new one.
     */
    fun reconnectSSE() {
        _state.update { it.copy(isReconnecting = true) }
        
        // Cancel existing SSE connection
        sseJob?.cancel()
        
        // Start new connection
        startRealTimeUpdates()
    }
    
    /**
     * Clear any error messages from the state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Get the current user session for authentication checks.
     */
    suspend fun getUserSession(): UserSession? {
        return sessionManager.getSession()
    }
    
    /**
     * Log out the current user and clear session data.
     */
    fun logout() {
        screenModelScope.launch {
            sessionManager.logout()
        }
    }
    
    // =================================
    // Private Implementation Methods
    // =================================
    
    /**
     * Start listening to real-time updates via SSE.
     * Automatically handles connection, reconnection, and error states.
     */
    private fun startRealTimeUpdates() {
        sseJob = screenModelScope.launch {
            try {
                repository.getOrdersRealTime().collect { event ->
                    when (event) {
                        is KitchenEvent.Connected -> {
                            _state.update { 
                                it.copy(
                                    isConnected = true,
                                    isReconnecting = false
                                )
                            }
                        }
                        is KitchenEvent.OrdersUpdate -> {
                            _state.update { 
                                it.copy(
                                    orders = event.orders,
                                    isLoading = false,
                                    isRefreshing = false,
                                    isConnected = true
                                )
                            }
                        }
                        is KitchenEvent.Heartbeat -> {
                            // Keep connection alive - ensure we're still connected
                            _state.update { it.copy(isConnected = true) }
                        }
                        is KitchenEvent.Error -> {
                            _state.update { 
                                it.copy(
                                    error = event.message,
                                    isConnected = false,
                                    isReconnecting = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = "Real-time connection failed: ${e.message}",
                        isConnected = false,
                        isReconnecting = false
                    )
                }
            }
        }
    }
    
    /**
     * Apply optimistic updates to order items for immediate UI feedback.
     * Automatically removes orders when all items are delivered.
     */
    private fun updateOrdersOptimistically(
        orders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ): List<KitchenOrder> {
        return orders.mapNotNull { order ->
            if (order.id == orderId) {
                val updatedItems = order.items.map { item ->
                    if (item.itemId == itemId) {
                        val updatedUnitStatuses =
                            item.unitStatuses.mapIndexed { index, unitStatus ->
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
        }
    }
    
    /**
     * Mark all units of an item as delivered optimistically.
     * Automatically removes orders when all items are delivered.
     */
    private fun markItemAsDeliveredOptimistically(
        orders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int
    ): List<KitchenOrder> {
        return orders.mapNotNull { order ->
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
        }
    }
    
    /**
     * Calculate the overall status of an item based on its unit statuses.
     * Returns DELIVERED if all units are delivered, otherwise OPEN.
     */
    private fun calculateOverallStatus(unitStatuses: List<ItemUnitStatus>): ItemStatus {
        return when {
            unitStatuses.all { it.status == ItemStatus.DELIVERED } -> ItemStatus.DELIVERED
            else -> ItemStatus.OPEN
        }
    }
}