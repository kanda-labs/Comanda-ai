package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory

data class ItemsManagementScreenState(
    val items: List<Item> = emptyList(),
    val filteredItems: List<Item> = emptyList(),
    val categories: List<ItemCategory> = emptyList(),
    val selectedCategory: ItemCategory? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCategoryModal: Boolean = false
)