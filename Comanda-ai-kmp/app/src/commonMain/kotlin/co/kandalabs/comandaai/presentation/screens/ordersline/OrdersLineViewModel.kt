package co.kandalabs.comandaai.presentation.screens.ordersline

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.data.api.OrderEvent
import co.kandalabs.comandaai.data.api.OrderSSEClient
import co.kandalabs.comandaai.domain.repository.OrderRepository
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class OrdersLineViewModel(
    private val orderRepository: OrderRepository,
    private val orderSSEClient: OrderSSEClient,
    private val sessionManager: SessionManager
) : StateScreenModel<OrdersLineScreenState>(OrdersLineScreenState()) {
    
    private var sseJob: Job? = null
    
    init {
        connectToSSE()
    }
    
    fun loadOrders() {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            
            orderRepository.getAllOrders()
                .fold(
                    onSuccess = { orders ->
                        mutableState.update { state ->
                            state.copy(
                                orders = orders.toPersistentList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        mutableState.update { state ->
                            state.copy(
                                isLoading = false,
                                error = error as? co.kandalabs.comandaai.core.error.ComandaAiException
                            )
                        }
                    }
                )
        }
    }
    
    fun refreshOrders() {
        screenModelScope.launch {
            mutableState.update { it.copy(isRefreshing = true) }
            
            orderRepository.getAllOrders()
                .fold(
                    onSuccess = { orders ->
                        mutableState.update { state ->
                            state.copy(
                                orders = orders.toPersistentList(),
                                isRefreshing = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        mutableState.update { state ->
                            state.copy(
                                isRefreshing = false,
                                error = error as? co.kandalabs.comandaai.core.error.ComandaAiException
                            )
                        }
                    }
                )
        }
    }
    
    fun markOrderAsCompleted(orderId: Int) {
        screenModelScope.launch {
            val order = state.value.orders.find { it.id == orderId } ?: return@launch
            val updatedOrder = order.copy(
                status = OrderStatus.GRANTED,
                items = order.items.map { it.copy(status = ItemStatus.GRANTED) }
            )
            
            orderRepository.updateOrder(orderId, updatedOrder)
                .onSuccess {
                    refreshOrders()
                }
        }
    }
    
    fun markItemAsCompleted(orderId: Int, itemId: Int) {
        screenModelScope.launch {
            val order = state.value.orders.find { it.id == orderId } ?: return@launch
            val updatedItems = order.items.map { item ->
                if (item.itemId == itemId) {
                    item.copy(status = ItemStatus.GRANTED)
                } else {
                    item
                }
            }
            
            // Check if all items are completed
            val allItemsCompleted = updatedItems.all { it.status == ItemStatus.GRANTED }
            val updatedOrder = order.copy(
                items = updatedItems,
                status = if (allItemsCompleted) OrderStatus.GRANTED else order.status
            )
            
            orderRepository.updateOrder(orderId, updatedOrder)
                .onSuccess {
                    refreshOrders()
                }
        }
    }
    
    fun selectOrder(orderId: Int?) {
        mutableState.update { it.copy(selectedOrderId = orderId) }
    }
    
    fun logout() {
        screenModelScope.launch {
            sessionManager.clearSession()
        }
    }
    
    suspend fun getUserSession() = sessionManager.getSession()
    
    private fun connectToSSE() {
        sseJob?.cancel()
        sseJob = screenModelScope.launch {
            orderSSEClient.connectToOrderEvents()
                .catch { error ->
                    mutableState.update { state ->
                        state.copy(
                            error = co.kandalabs.comandaai.core.error.ComandaAiException.UnknownException(
                                message = "Connection error: ${error.message}"
                            )
                        )
                    }
                    // Retry connection after 5 seconds
                    kotlinx.coroutines.delay(5000)
                    connectToSSE()
                }
                .onCompletion {
                    // Reconnect if connection was lost
                    if (it == null) {
                        kotlinx.coroutines.delay(5000)
                        connectToSSE()
                    }
                }
                .collect { event ->
                    when (event) {
                        is OrderEvent.Connected -> {
                            mutableState.update { it.copy(error = null) }
                        }
                        is OrderEvent.OrdersUpdate -> {
                            mutableState.update { state ->
                                state.copy(
                                    orders = event.orders.toPersistentList(),
                                    isLoading = false,
                                    error = null
                                )
                            }
                        }
                        is OrderEvent.Error -> {
                            mutableState.update { state ->
                                state.copy(
                                    error = co.kandalabs.comandaai.core.error.ComandaAiException.UnknownException(
                                        message = event.message
                                    )
                                )
                            }
                        }
                        is OrderEvent.Heartbeat -> {
                            // Just a heartbeat, no action needed
                        }
                    }
                }
        }
    }
    
    override fun onDispose() {
        sseJob?.cancel()
        super.onDispose()
    }
}
