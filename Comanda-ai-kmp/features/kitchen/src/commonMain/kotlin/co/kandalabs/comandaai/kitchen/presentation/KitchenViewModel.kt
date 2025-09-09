package co.kandalabs.comandaai.kitchen.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.sdk.session.LogoutManager
import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.sdk.session.UserSession
import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.domain.ItemStatus
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
                            activeOrders = orders,
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
     * Load delivered orders from the repository.
     */
    fun loadDeliveredOrders() {
        _state.update { it.copy(isLoading = true, error = null) }
        
        screenModelScope.launch {
            repository.getDeliveredOrders()
                .onSuccess { orders ->
                    _state.update { 
                        it.copy(
                            deliveredOrders = orders,
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
     * Switch between active and delivered orders filter.
     */
    fun switchOrderFilter(filter: OrderFilter) {
        _state.update { it.copy(currentFilter = filter) }
        
        // Load appropriate orders based on filter
        when (filter) {
            OrderFilter.ACTIVE -> {
                if (_state.value.activeOrders.isEmpty()) {
                    loadActiveOrders()
                }
            }
            OrderFilter.DELIVERED -> {
                if (_state.value.deliveredOrders.isEmpty()) {
                    loadDeliveredOrders()
                }
            }
        }
    }
    
    /**
     * Update the status of a specific unit of an item in an order.
     * Uses optimistic updates for better UX.
     * Handles moving orders between active and delivered lists.
     */
    fun updateItemStatus(
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ) {
        val itemKey = "$orderId-$itemId"
        
        // Add to loading items
        _state.update { 
            it.copy(loadingItemIds = it.loadingItemIds + itemKey)
        }
        
        // Make API call
        screenModelScope.launch {
            repository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus)
                .onSuccess {
                    // Success: Update orders and remove from loading
                    val currentState = _state.value
                    val (updatedActiveOrders, updatedDeliveredOrders) = updateOrdersWithFilter(
                        currentState.activeOrders, currentState.deliveredOrders, orderId, itemId, unitIndex, newStatus
                    )
                    
                    _state.update { 
                        it.copy(
                            activeOrders = updatedActiveOrders,
                            deliveredOrders = updatedDeliveredOrders,
                            loadingItemIds = it.loadingItemIds - itemKey
                        )
                    }
                }
                .onFailure { error ->
                    // Error: Remove from loading and show error
                    _state.update { 
                        it.copy(
                            loadingItemIds = it.loadingItemIds - itemKey,
                            error = "Falha ao atualizar item. Tente novamente."
                        )
                    }
                }
        }
    }
    
    /**
     * Mark an entire order as delivered.
     * Moves the order from active to delivered list.
     */
    fun markOrderAsDelivered(orderId: Int) {
        screenModelScope.launch {
            repository.markOrderAsDelivered(orderId)
                .onSuccess {
                    // Move order from active to delivered list
                    _state.update { currentState ->
                        val orderToMove = currentState.activeOrders.find { it.id == orderId }
                        if (orderToMove != null) {
                            // Mark all non-canceled items as delivered
                            val deliveredOrder = orderToMove.copy(
                                items = orderToMove.items.map { item ->
                                    item.copy(
                                        overallStatus = if (item.overallStatus == ItemStatus.CANCELED) ItemStatus.CANCELED else ItemStatus.DELIVERED,
                                        unitStatuses = item.unitStatuses.map { unit ->
                                            unit.copy(status = if (unit.status == ItemStatus.CANCELED) ItemStatus.CANCELED else ItemStatus.DELIVERED)
                                        }
                                    )
                                }
                            )
                            
                            currentState.copy(
                                activeOrders = currentState.activeOrders.filter { it.id != orderId },
                                deliveredOrders = currentState.deliveredOrders + deliveredOrder
                            )
                        } else {
                            currentState
                        }
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
     * Handles moving orders between active and delivered lists.
     */
    fun markItemAsDelivered(orderId: Int, itemId: Int) {
        val itemKey = "$orderId-$itemId"
        
        // Add to loading items
        _state.update { 
            it.copy(loadingItemIds = it.loadingItemIds + itemKey)
        }
        
        // Make API call
        screenModelScope.launch {
            repository.markItemAsDelivered(orderId, itemId)
                .onSuccess {
                    // Success: Update orders and remove from loading
                    val currentState = _state.value
                    val (updatedActiveOrders, updatedDeliveredOrders) = markItemAsDeliveredWithFilter(
                        currentState.activeOrders, currentState.deliveredOrders, orderId, itemId
                    )
                    
                    _state.update { 
                        it.copy(
                            activeOrders = updatedActiveOrders,
                            deliveredOrders = updatedDeliveredOrders,
                            loadingItemIds = it.loadingItemIds - itemKey
                        )
                    }
                }
                .onFailure { error ->
                    // Error: Remove from loading and show error
                    _state.update { 
                        it.copy(
                            loadingItemIds = it.loadingItemIds - itemKey,
                            error = "Falha ao entregar item. Tente novamente."
                        )
                    }
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
            
            // Refresh based on current filter
            when (_state.value.currentFilter) {
                OrderFilter.ACTIVE -> {
                    repository.getActiveOrders()
                        .onSuccess { orders ->
                            _state.update { 
                                it.copy(
                                    activeOrders = orders,
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
                OrderFilter.DELIVERED -> {
                    repository.getDeliveredOrders()
                        .onSuccess { orders ->
                            _state.update { 
                                it.copy(
                                    deliveredOrders = orders,
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
        // Use LogoutManager to prevent "Parent job is Completed" errors
        LogoutManager.performLogout(sessionManager)
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
                                    activeOrders = event.orders,
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
                            unitStatus.copy(status = if (unitStatus.status == ItemStatus.CANCELED) ItemStatus.CANCELED else ItemStatus.DELIVERED)
                        }
                        // Calculate overall status considering canceled items
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
     * Calculate the overall status of an item based on its unit statuses.
     * Returns CANCELED if all units are canceled, DELIVERED if all non-canceled units are delivered, otherwise PENDING.
     */
    private fun calculateOverallStatus(unitStatuses: List<ItemUnitStatus>): ItemStatus {
        return when {
            unitStatuses.all { it.status == ItemStatus.CANCELED } -> ItemStatus.CANCELED
            unitStatuses.all { it.status == ItemStatus.DELIVERED || it.status == ItemStatus.CANCELED } -> ItemStatus.DELIVERED
            else -> ItemStatus.PENDING
        }
    }
    
    /**
     * Apply updates to orders with filter support.
     * Handles moving orders between active and delivered lists.
     */
    private fun updateOrdersWithFilter(
        activeOrders: List<KitchenOrder>,
        deliveredOrders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ): Pair<List<KitchenOrder>, List<KitchenOrder>> {
        
        // Find order in active list first
        val orderInActive = activeOrders.find { it.id == orderId }
        if (orderInActive != null) {
            return updateOrderInActiveList(activeOrders, deliveredOrders, orderId, itemId, unitIndex, newStatus)
        }
        
        // If not found in active, look in delivered list
        val orderInDelivered = deliveredOrders.find { it.id == orderId }
        if (orderInDelivered != null) {
            return updateOrderInDeliveredList(activeOrders, deliveredOrders, orderId, itemId, unitIndex, newStatus)
        }
        
        // Order not found in either list, return unchanged
        return Pair(activeOrders, deliveredOrders)
    }
    
    /**
     * Update an order in the active list, potentially moving it to delivered.
     */
    private fun updateOrderInActiveList(
        activeOrders: List<KitchenOrder>,
        deliveredOrders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ): Pair<List<KitchenOrder>, List<KitchenOrder>> {
        var updatedActiveOrders = activeOrders
        var updatedDeliveredOrders = deliveredOrders
        
        updatedActiveOrders = activeOrders.mapNotNull { order ->
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

                // Check if all items are delivered and move order if so
                val allItemsDelivered = updatedItems.all { item ->
                    item.unitStatuses.all { it.status == ItemStatus.DELIVERED }
                }
                

                if (allItemsDelivered) {
                    // Move to delivered list
                    val deliveredOrder = order.copy(items = updatedItems)
                    updatedDeliveredOrders = updatedDeliveredOrders + deliveredOrder
                    null // Will be filtered out from active
                } else {
                    order.copy(items = updatedItems)
                }
            } else {
                order
            }
        }
        
        return Pair(updatedActiveOrders, updatedDeliveredOrders)
    }
    
    /**
     * Update an order in the delivered list, potentially moving it back to active.
     */
    private fun updateOrderInDeliveredList(
        activeOrders: List<KitchenOrder>,
        deliveredOrders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int,
        unitIndex: Int,
        newStatus: ItemStatus
    ): Pair<List<KitchenOrder>, List<KitchenOrder>> {
        var updatedActiveOrders = activeOrders
        var updatedDeliveredOrders = deliveredOrders
        
        updatedDeliveredOrders = deliveredOrders.mapNotNull { order ->
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

                // Check if any item is no longer delivered (revert to active)
                val hasNonDeliveredItems = updatedItems.any { item ->
                    item.unitStatuses.any { it.status != ItemStatus.DELIVERED }
                }

                if (hasNonDeliveredItems && newStatus != ItemStatus.DELIVERED) {
                    // Move back to active list
                    val activeOrder = order.copy(items = updatedItems)
                    updatedActiveOrders = updatedActiveOrders + activeOrder
                    null // Will be filtered out from delivered
                } else {
                    order.copy(items = updatedItems)
                }
            } else {
                order
            }
        }
        
        return Pair(updatedActiveOrders, updatedDeliveredOrders)
    }
    
    /**
     * Mark all units of an item as delivered with filter support.
     */
    private fun markItemAsDeliveredWithFilter(
        activeOrders: List<KitchenOrder>,
        deliveredOrders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int
    ): Pair<List<KitchenOrder>, List<KitchenOrder>> {
        
        // Find order in active list first
        val orderInActive = activeOrders.find { it.id == orderId }
        if (orderInActive != null) {
            return markItemAsDeliveredInActiveList(activeOrders, deliveredOrders, orderId, itemId)
        }
        
        // If not found in active, return unchanged
        return Pair(activeOrders, deliveredOrders)
    }
    
    /**
     * Mark item as delivered in active list, potentially moving order to delivered.
     */
    private fun markItemAsDeliveredInActiveList(
        activeOrders: List<KitchenOrder>,
        deliveredOrders: List<KitchenOrder>,
        orderId: Int,
        itemId: Int
    ): Pair<List<KitchenOrder>, List<KitchenOrder>> {
        var updatedActiveOrders = activeOrders
        var updatedDeliveredOrders = deliveredOrders
        
        updatedActiveOrders = activeOrders.mapNotNull { order ->
            if (order.id == orderId) {
                val updatedItems = order.items.map { item ->
                    if (item.itemId == itemId) {
                        val updatedUnitStatuses = item.unitStatuses.map { unitStatus ->
                            unitStatus.copy(status = if (unitStatus.status == ItemStatus.CANCELED) ItemStatus.CANCELED else ItemStatus.DELIVERED)
                        }
                        // Calculate overall status considering canceled items
                        val newOverallStatus = calculateOverallStatus(updatedUnitStatuses)
                        item.copy(
                            unitStatuses = updatedUnitStatuses,
                            overallStatus = newOverallStatus
                        )
                    } else {
                        item
                    }
                }

                // Check if all items are delivered and move order if so
                val allItemsDelivered = updatedItems.all { item ->
                    item.unitStatuses.all { it.status == ItemStatus.DELIVERED }
                }
                

                if (allItemsDelivered) {
                    // Move to delivered list
                    val deliveredOrder = order.copy(items = updatedItems)
                    updatedDeliveredOrders = updatedDeliveredOrders + deliveredOrder
                    null // Will be filtered out from active
                } else {
                    order.copy(items = updatedItems)
                }
            } else {
                order
            }
        }
        
        return Pair(updatedActiveOrders, updatedDeliveredOrders)
    }
    
}