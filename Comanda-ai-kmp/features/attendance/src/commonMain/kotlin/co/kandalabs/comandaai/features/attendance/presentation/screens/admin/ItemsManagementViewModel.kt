package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ItemsManagementViewModel(
    private val itemsRepository: ItemsRepository,
    private val logger: ComandaAiLogger
) : ScreenModel {

    private val _uiState = MutableStateFlow(ItemsManagementScreenState())
    val uiState: StateFlow<ItemsManagementScreenState> = _uiState.asStateFlow()

    init {
        loadItems()
    }

    fun loadItems() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            itemsRepository.getItems(null)
                .onSuccess { items ->
                    val categories = items.map { it.category }.distinct()
                    val filteredItems = if (_uiState.value.selectedCategory == null) {
                        items
                    } else {
                        items.filter { it.category == _uiState.value.selectedCategory }
                    }

                    _uiState.update {
                        it.copy(
                            items = items,
                            filteredItems = filteredItems,
                            categories = categories,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    logger.e(error, "Failed to load items")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erro ao carregar itens"
                        )
                    }
                }
        }
    }

    fun selectCategory(category: ItemCategory?) {
        _uiState.update { state ->
            val filteredItems = if (category == null) {
                state.items
            } else {
                state.items.filter { it.category == category }
            }

            state.copy(
                selectedCategory = category,
                filteredItems = filteredItems
            )
        }
    }

    fun showCategoryFilter() {
        _uiState.update { it.copy(showCategoryModal = true) }
    }

    fun hideCategoryFilter() {
        _uiState.update { it.copy(showCategoryModal = false) }
    }
}