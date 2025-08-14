package co.touchlab.dogify.presentation.screens.order

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.dogify.core.coroutinesResult.DogifyResult
import co.touchlab.dogify.domain.model.ItemWithCount
import co.touchlab.dogify.domain.repository.CreateOrderItemRequest
import co.touchlab.dogify.domain.repository.ItemsRepository
import co.touchlab.dogify.domain.repository.OrderRepository
import kandalabs.commander.domain.model.Item
import kandalabs.commander.domain.model.ItemCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class OrderScreenModel(
    private val itemsRepository: ItemsRepository,
    private val orderRepository: OrderRepository
) : ScreenModel {
    
    private val _allItems = MutableStateFlow<List<Item>>(emptyList())
    private val _selectedCategory = MutableStateFlow(ItemCategory.DRINK)
    private val _selectedItems = MutableStateFlow<Map<Int, Int>>(emptyMap()) // itemId -> count
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _orderSubmitted = MutableStateFlow(false)
    private val _showConfirmationModal = MutableStateFlow(false)
    private val _isSubmitting = MutableStateFlow(false)
    
    val categories = ItemCategory.values().toList()
    val selectedCategory = _selectedCategory.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val error = _error.asStateFlow()
    val orderSubmitted = _orderSubmitted.asStateFlow()
    val showConfirmationModal = _showConfirmationModal.asStateFlow()
    val isSubmitting = _isSubmitting.asStateFlow()
    
    val filteredItems = combine(_allItems, _selectedCategory) { items, category ->
        items.filter { it.category == category }
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
        screenModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            try {
                val orderItems = _selectedItems.value.mapNotNull { (itemId, count) ->
                    val item = _allItems.value.find { it.id == itemId }
                    item?.let {
                        CreateOrderItemRequest(
                            itemId = itemId,
                            name = it.name,
                            count = count,
                            observation = null
                        )
                    }
                }
                
                val result = orderRepository.createOrder(
                    tableId = tableId,
                    billId = billId,
                    items = orderItems
                )
                
                when (result) {
                    is DogifyResult.Success -> {
                        _selectedItems.value = emptyMap()
                        _orderSubmitted.value = true
                        _showConfirmationModal.value = false
                    }
                    is DogifyResult.Failure -> {
                        _error.value = "Erro ao criar pedido: ${result.exception.message}"
                        _showConfirmationModal.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro inesperado: ${e.message}"
                _showConfirmationModal.value = false
            } finally {
                _isSubmitting.value = false
            }
        }
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
    
    private fun loadItems() {
        screenModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = itemsRepository.getItems(itemStatus = null)
                when (result) {
                    is DogifyResult.Success -> {
                        _allItems.value = result.data
                    }
                    is DogifyResult.Failure -> {
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