package co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.core.coroutinesResult.ComandaAiResult
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

internal class BreedsListingViewModel(
    private val repository: ItemsRepository,
) : StateScreenModel<ItemsSelectionScreenState>(ItemsSelectionScreenState()) {
    fun retrieveItems() {
        screenModelScope.launch {
            if (mutableState.value.Items.isEmpty()) {
                mutableState.emit(ItemsSelectionScreenState(isLoading = true))
            }

            val result = repository.getItems(itemStatus = null)
            when (result) {
                is ComandaAiResult.Success -> {
                    mutableState.emit(
                        ItemsSelectionScreenState(
                            Items = result.data.toPersistentList(),
                            isLoading = false,
                            error = null
                        )
                    )
                }
                is ComandaAiResult.Failure -> {
                    mutableState.emit(
                        ItemsSelectionScreenState(isLoading = false, error = result.exception)
                    )
                }
            }
        }
    }
}
