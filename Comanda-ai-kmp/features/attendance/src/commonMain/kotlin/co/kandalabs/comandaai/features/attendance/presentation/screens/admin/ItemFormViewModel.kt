package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.features.attendance.domain.repository.ItemsRepository
import co.kandalabs.comandaai.core.utils.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemFormState(
    val itemId: Int? = null,
    val name: String = "",
    val description: String = "",
    val priceDisplay: String = "",
    val priceInCents: Int = 0,
    val category: ItemCategory? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val priceError: String? = null,
    val categoryError: String? = null
)

internal class ItemFormViewModel(
    private val itemsRepository: ItemsRepository,
    private val logger: ComandaAiLogger
) : ScreenModel {

    private val _uiState = MutableStateFlow(ItemFormState())
    val uiState: StateFlow<ItemFormState> = _uiState.asStateFlow()


    fun loadItem(itemId: Int) {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            itemsRepository.getItemById(itemId)
                .onSuccess { item ->
                    _uiState.update {
                        it.copy(
                            itemId = item.id,
                            name = item.name,
                            description = item.description ?: "",
                            priceDisplay = CurrencyFormatter.formatDecimal(item.value / 100.0),
                            priceInCents = item.value,
                            category = item.category,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    logger.e(error, "Failed to load item")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao carregar item: ${error.message}"
                        )
                    }
                }
        }
    }

    fun updateName(value: String) {
        _uiState.update {
            it.copy(
                name = value,
                nameError = null
            )
        }
    }

    fun updateDescription(value: String) {
        _uiState.update {
            it.copy(description = value)
        }
    }

    fun updatePrice(value: String) {
        val priceInCents = CurrencyFormatter.parseToCents(value)

        _uiState.update {
            it.copy(
                priceDisplay = value,
                priceInCents = priceInCents,
                priceError = null
            )
        }
    }

    fun updateCategory(category: ItemCategory) {
        _uiState.update {
            it.copy(
                category = category,
                categoryError = null
            )
        }
    }

    fun saveItem(onSuccess: () -> Unit) {
        if (!validateForm()) return

        screenModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val item = Item(
                id = _uiState.value.itemId,
                name = _uiState.value.name,
                description = _uiState.value.description.ifEmpty { null },
                value = _uiState.value.priceInCents,
                category = _uiState.value.category!!
            )

            val result = if (item.id == null) {
                itemsRepository.createItem(item)
            } else {
                itemsRepository.updateItem(item.id!!, item)
            }

            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    logger.e(error, "Failed to save item")
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Erro ao salvar item: ${error.message}"
                        )
                    }
                }
        }
    }

    fun deleteItem(onSuccess: () -> Unit) {
        val itemId = _uiState.value.itemId ?: return

        screenModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            itemsRepository.deleteItem(itemId)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess()
                }
                .onFailure { error ->
                    logger.e(error, "Failed to delete item")
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = "Erro ao deletar item: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Nome é obrigatório") }
            isValid = false
        }

        if (_uiState.value.priceInCents < 0) {
            _uiState.update { it.copy(priceError = "Preço não pode ser negativo") }
            isValid = false
        }

        if (_uiState.value.category == null) {
            _uiState.update { it.copy(categoryError = "Categoria é obrigatória") }
            isValid = false
        }

        return isValid
    }

}