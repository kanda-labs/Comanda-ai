package co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.domain.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ItemsSelectionScreenState(
    val isLoading: Boolean = true,
    val Items: ImmutableList<Item> = persistentListOf(),
    val selectedItems: ImmutableList<Item> = persistentListOf(),
    val error: ComandaAiException? = null
)
