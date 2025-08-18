package co.kandalabs.comandaai.presentation.screens.tables.details

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.domain.repository.TablesRepository
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

internal class TablesDetailsViewModel(
    private val repository: TablesRepository,
) : StateScreenModel<TableDetailsScreenState>(TableDetailsScreenState()) {

    fun setupDetails(table: Table) {
        screenModelScope.launch {
            mutableState.emit(TableDetailsScreenState(isLoading = true))

            safeRunCatching {
                val updatedTable = table.id?.let { tableId ->
                    repository.getTableById(tableId).fold(
                        onSuccess = { it },
                        onFailure = { table }
                    )
                } ?: table
                TableDetailsScreenState(table = updatedTable, isLoading = false)
            }.fold(
                onSuccess = { tableDetailsScreenState ->
                    mutableState.emit(tableDetailsScreenState)
                },
                onFailure = { error ->
                    mutableState.emit(
                        TableDetailsScreenState(isLoading = false, error = error)
                    )
                }
            )
        }
    }

    fun openTable(table: Table) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            safeRunCatching {
                repository.openTable(tableId, table.number)
            }.fold(
                onSuccess = {
                    // Fetch updated table data from server to reflect status change
                    refreshTableData(tableId)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(error = error as? co.kandalabs.comandaai.core.error.ComandaAiException)
                    )
                }
            )
        }
    }

    private fun refreshTableData(tableId: Int) {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(isLoading = true))
            
            repository.getTableById(tableId).fold(
                onSuccess = { updatedTable ->
                    setupDetails(updatedTable)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(isLoading = false, error = error as? co.kandalabs.comandaai.core.error.ComandaAiException)
                    )
                }
            )
        }
    }

    fun closeTable(table: Table) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            safeRunCatching {
                repository.closeTable(tableId)
            }.fold(
                onSuccess = {
                    // Fetch updated table data from server to reflect status change
                    refreshTableData(tableId)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(error = error as? co.kandalabs.comandaai.core.error.ComandaAiException)
                    )
                }
            )
        }
    }

    fun showOrderDetails(order: Order) {
        screenModelScope.launch {
            mutableState.emit(
                state.value.copy(selectedOrderForDetails = order)
            )
        }
    }

    fun hideOrderDetails() {
        screenModelScope.launch {
            mutableState.emit(
                state.value.copy(selectedOrderForDetails = null)
            )
        }
    }

    fun refreshData() {
        val currentTable = state.value.currentTable
        if (currentTable != null) {
            setupDetails(currentTable)
        }
    }
}
