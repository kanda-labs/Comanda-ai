package co.kandalabs.comandaai.presentation.screens.itemsSelection

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.domain.repository.ItemsRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

internal class BreedsListingViewModel(
    private val repository: ItemsRepository,
) : StateScreenModel<ItemsSelectionScreenState>(ItemsSelectionScreenState()) {
    fun retrieveBreeds() {
        screenModelScope.launch {
            if (mutableState.value.Items.isEmpty()) {
                mutableState.emit(ItemsSelectionScreenState(isLoading = true))
            }

            repository.getItems(itemStatus = null)
                .fold(
                    onSuccess = { items ->
                        mutableState.emit(
                            ItemsSelectionScreenState(
                                Items = items.toPersistentList(),
                                isLoading = false,
                                error = null
                            )
                        )
                    },
                    onFailure = { error ->
                        mutableState.emit(
                            ItemsSelectionScreenState(isLoading = false, error = error)
                        )
                    }
                )
        }
    }
}
