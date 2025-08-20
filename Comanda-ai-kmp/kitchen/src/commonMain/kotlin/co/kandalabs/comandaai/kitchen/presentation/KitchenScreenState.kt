package co.kandalabs.comandaai.kitchen.presentation

import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder

data class KitchenScreenState(
    val orders: List<KitchenOrder> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val isConnected: Boolean = false,
    val isReconnecting: Boolean = false
)