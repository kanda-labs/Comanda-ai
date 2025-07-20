package co.touchlab.dogify.presentation.screens.itemsSelection

import co.touchlab.dogify.core.error.ComandaAiException
import kandalabs.commander.domain.model.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class ItemsSelectionScreenState(
    val isLoading: Boolean = true,
    val Items: ImmutableList<Item> = persistentListOf(),
    val selectedItems: ImmutableList<Item> = persistentListOf(),
    val error: ComandaAiException? = null
)
