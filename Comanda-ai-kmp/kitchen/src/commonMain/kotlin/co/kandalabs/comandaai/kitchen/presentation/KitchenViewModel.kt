package co.kandalabs.comandaai.kitchen.presentation

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.kitchen.data.api.KitchenEvent
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.repository.KitchenRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KitchenViewModel(
    private val repository: KitchenRepository
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
        screenModelScope.launch {
            repository.updateItemUnitStatus(orderId, itemId, unitIndex, newStatus)
                .onSuccess {
                    // Recarregar pedidos para refletir mudanças
                    loadActiveOrders()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
        }
    }
    
    fun markOrderAsDelivered(orderId: Int) {
        screenModelScope.launch {
            repository.markOrderAsDelivered(orderId)
                .onSuccess {
                    // Recarregar pedidos para refletir mudanças
                    loadActiveOrders()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
        }
    }
    
    fun markItemAsDelivered(orderId: Int, itemId: Int) {
        screenModelScope.launch {
            repository.markItemAsDelivered(orderId, itemId)
                .onSuccess {
                    // Recarregar pedidos para refletir mudanças
                    loadActiveOrders()
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
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
}