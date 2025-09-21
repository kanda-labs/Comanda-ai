package co.kandalabs.comandaai.features.attendance.presentation.screens.order

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.sdk.coroutinesResult.ComandaAiResult
import co.kandalabs.comandaai.sdk.session.SessionManager
import co.kandalabs.comandaai.features.attendance.domain.model.ItemWithCount
import co.kandalabs.comandaai.features.attendance.domain.models.request.CreateOrderItemRequest
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.features.attendance.domain.repository.OrderRepository
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessDrinksUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecase.ProcessPromotionalItemsUseCase
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted

class OrderScreenModel(
    private val itemsRepository: ItemsRepository,
    private val orderRepository: OrderRepository,
    private val sessionManager: SessionManager,
    private val processPromotionalItemsUseCase: ProcessPromotionalItemsUseCase,
    private val processDrinksUseCase: ProcessDrinksUseCase,
) : ScreenModel {
    
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    private val _selectedCategory = MutableStateFlow(ItemCategory.SKEWER)
    private val _selectedItems = MutableStateFlow<Map<Int, Int>>(emptyMap()) // itemId -> count
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _orderSubmitted = MutableStateFlow(false)
    private val _showConfirmationModal = MutableStateFlow(false)
    private val _orderSubmissionState = MutableStateFlow(OrderSubmissionState.IDLE)
    private val _orderSubmissionError = MutableStateFlow<String?>(null)
    private val _showObservationModal = MutableStateFlow(false)
    private val _selectedItemForObservation = MutableStateFlow<Item?>(null)
    private val _itemObservations = MutableStateFlow<Map<Int, String>>(emptyMap()) // itemId -> observation
    
    val categories = ItemCategory.values().toList()
    val selectedCategory = _selectedCategory.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val error = _error.asStateFlow()
    val orderSubmitted = _orderSubmitted.asStateFlow()
    val showConfirmationModal = _showConfirmationModal.asStateFlow()
    val orderSubmissionState = _orderSubmissionState.asStateFlow()
    val orderSubmissionError = _orderSubmissionError.asStateFlow()
    val showObservationModal = _showObservationModal.asStateFlow()
    val selectedItemForObservation = _selectedItemForObservation.asStateFlow()
    val itemObservations = _itemObservations.asStateFlow()
    
    val currentObservationForSelectedItem = combine(
        _selectedItemForObservation,
        _itemObservations
    ) { item, observations ->
        item?.id?.let { observations[it] }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), null)
    
    val selectedItemHasQuantity = combine(
        _selectedItemForObservation,
        _selectedItems
    ) { item, selectedItems ->
        item?.id?.let { selectedItems[it] ?: 0 > 0 } ?: false
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), false)
    
    val filteredItems = combine(_allItems, _selectedCategory) { items, category ->
        items.filter { it.category == category && it.value > 0 }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val itemsWithCount = combine(filteredItems, _selectedItems) { items, selected ->
        items.map { item ->
            ItemWithCount(item, selected[item.id] ?: 0)
        }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val canSubmit = _selectedItems.map { selectedItems ->
        selectedItems.values.sum() > 0
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), false)
    
    val totalItems = _selectedItems.map { selectedItems ->
        selectedItems.values.sum()
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), 0)
    
    val selectedItemsWithCount = combine(_allItems, _selectedItems) { allItems, selected ->
        selected.mapNotNull { (itemId, count) ->
            allItems.find { it.id == itemId }?.let { item ->
                ItemWithCount(item, count)
            }
        }
    }.stateIn(screenModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    init {
        loadItems()
    }
    
    fun selectCategory(category: ItemCategory) {
        _selectedCategory.value = category
    }
    
    fun incrementItem(itemId: Int) {
        val current = _selectedItems.value.toMutableMap()
        current[itemId] = (current[itemId] ?: 0) + 1
        _selectedItems.value = current
    }
    
    fun decrementItem(itemId: Int) {
        val current = _selectedItems.value.toMutableMap()
        val newCount = (current[itemId] ?: 0) - 1
        if (newCount <= 0) {
            current.remove(itemId)
        } else {
            current[itemId] = newCount
        }
        _selectedItems.value = current
    }
    
    fun submitOrder(tableId: Int, billId: Int) {
        if (_orderSubmissionState.value == OrderSubmissionState.LOADING) {
            return
        }

        screenModelScope.launch {
            _orderSubmissionState.value = OrderSubmissionState.LOADING
            _orderSubmissionError.value = null

            try {
                coroutineScope {

                    val operationResult = async {
                        processOrder(tableId, billId)
                    }

                    val result = operationResult.await()

                    when (result) {
                        is OrderResult.Success -> {
                            _selectedItems.value = emptyMap()
                            _itemObservations.value = emptyMap()
                            _orderSubmissionState.value = OrderSubmissionState.SUCCESS
                        }
                        is OrderResult.Error -> {
                            _orderSubmissionState.value = OrderSubmissionState.ERROR
                            _orderSubmissionError.value = result.message
                        }
                    }
                }
            } catch (e: Exception) {
                _orderSubmissionState.value = OrderSubmissionState.ERROR
                _orderSubmissionError.value = "Erro inesperado: ${e.message}"
            }
        }
    }

    private suspend fun processOrder(tableId: Int, billId: Int): OrderResult {
        return try {
            val orderItems = _selectedItems.value.mapNotNull { (itemId, count) ->
                val item = _allItems.value.find { it.id == itemId }
                item?.let {
                    CreateOrderItemRequest(
                        itemId = itemId,
                        name = it.name,
                        count = count,
                        observation = _itemObservations.value[itemId]
                    )
                }
            }

            // Processar itens promocionais automaticamente
            val promotionalResult = processPromotionalItemsUseCase.processPromotionalItems(orderItems)

            val userSession = sessionManager.getSession()
            val userName = userSession?.userName ?: ""

            // Processar bebidas separadamente
            val drinksResult = processDrinksUseCase.processDrinks(
                selectedItems = promotionalResult.processedItems,
                tableId = tableId,
                billId = billId,
                userName = userName
            )

            when (drinksResult) {
                is ComandaAiResult.Failure -> {
                    OrderResult.Error("Erro ao processar bebidas: ${drinksResult.exception.message}")
                }
                is ComandaAiResult.Success -> {
                    val drinksProcessResult = drinksResult.data

                    // Se não há itens para a cozinha, não criar pedido da cozinha
                    if (drinksProcessResult.kitchenItems.isEmpty()) {
                        // Processar apenas itens promocionais se houver
                        if (promotionalResult.promotionalItemIds.isNotEmpty() && drinksProcessResult.drinkOrder != null) {
                            processPromotionalItemsUseCase.markPromotionalItemsAsDelivered(
                                order = drinksProcessResult.drinkOrder,
                                promotionalItemIds = promotionalResult.promotionalItemIds,
                                updatedBy = userName
                            )
                        }
                        OrderResult.Success
                    } else {
                        // Criar pedido da cozinha apenas com os itens que não são bebidas
                        val result = orderRepository.createOrder(
                            tableId = tableId,
                            billId = billId,
                            userName = userName,
                            items = drinksProcessResult.kitchenItems
                        )

                        when (result) {
                            is ComandaAiResult.Success -> {
                                val createdOrder = result.data

                                // Marcar itens promocionais como DELIVERED se existirem
                                if (promotionalResult.promotionalItemIds.isNotEmpty()) {
                                    processPromotionalItemsUseCase.markPromotionalItemsAsDelivered(
                                        order = createdOrder,
                                        promotionalItemIds = promotionalResult.promotionalItemIds,
                                        updatedBy = userName
                                    )
                                }
                                OrderResult.Success
                            }
                            is ComandaAiResult.Failure -> {
                                OrderResult.Error("Erro ao criar pedido: ${result.exception.message}")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            OrderResult.Error("Erro inesperado: ${e.message}")
        }
    }

    private sealed class OrderResult {
        object Success : OrderResult()
        data class Error(val message: String) : OrderResult()
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun resetOrderSubmitted() {
        _orderSubmitted.value = false
    }
    
    fun showConfirmationModal() {
        _showConfirmationModal.value = true
    }
    
    fun hideConfirmationModal() {
        _showConfirmationModal.value = false
    }

    fun resetOrderSubmissionState() {
        _orderSubmissionState.value = OrderSubmissionState.IDLE
        _orderSubmissionError.value = null
    }
    
    fun showObservationModal(item: Item) {
        _selectedItemForObservation.value = item
        _showObservationModal.value = true
    }
    
    fun hideObservationModal() {
        _showObservationModal.value = false
        _selectedItemForObservation.value = null
    }
    
    fun addItemWithObservation(observation: String) {
        val item = _selectedItemForObservation.value
        if (item?.id != null) {
            // Add or update observation
            val currentObservations = _itemObservations.value.toMutableMap()
            val itemId = item.id ?: return
            if (observation.isNotBlank()) {
                currentObservations[itemId] = observation
            } else {
                currentObservations.remove(itemId)
            }
            _itemObservations.value = currentObservations
            
            // Only add item if quantity is 0
            val currentSelectedItems = _selectedItems.value
            val currentQuantity = currentSelectedItems[itemId] ?: 0
            if (currentQuantity == 0) {
                incrementItem(itemId)
            }
            
            // Hide modal
            hideObservationModal()
        }
    }
    
    private fun loadItems() {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = itemsRepository.getItems(itemStatus = null)
                when (result) {
                    is ComandaAiResult.Success -> {
                        _allItems.value = result.data
                    }
                    is ComandaAiResult.Failure -> {
                        _error.value = "Erro ao carregar itens: ${result.exception.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro inesperado ao carregar itens: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}