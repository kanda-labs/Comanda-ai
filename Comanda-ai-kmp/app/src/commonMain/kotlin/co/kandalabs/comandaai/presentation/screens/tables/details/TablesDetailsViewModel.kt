package co.kandalabs.comandaai.presentation.screens.tables.details

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.domain.repository.TablesRepository
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.models.model.Table
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

internal class TablesDetailsViewModel(
    private val repository: TablesRepository,
    private val sessionManager: SessionManager,
) : StateScreenModel<TableDetailsScreenState>(TableDetailsScreenState()) {

    fun setupDetails(table: Table) {
        screenModelScope.launch {
            mutableState.emit(TableDetailsScreenState(isLoading = true))

            safeRunCatching {
                val userSession = sessionManager.getSession()
                val updatedTable = table.id?.let { tableId ->
                    repository.getTableById(tableId).fold(
                        onSuccess = { it },
                        onFailure = { table }
                    )
                } ?: table
                TableDetailsScreenState(table = updatedTable, userSession = userSession, isLoading = false)
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
                    // Hide confirmation dialog and fetch updated table data
                    mutableState.emit(state.value.copy(showCloseTableConfirmation = false))
                    refreshTableData(tableId)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(
                            error = error as? co.kandalabs.comandaai.core.error.ComandaAiException,
                            showCloseTableConfirmation = false
                        )
                    )
                }
            )
        }
    }


    fun setupDetailsById(tableId: Int) {
        screenModelScope.launch {
            mutableState.emit(TableDetailsScreenState(isLoading = true))

            safeRunCatching {
                val userSession = sessionManager.getSession()
                val table = repository.getTableById(tableId).fold(
                    onSuccess = { it },
                    onFailure = { throw it }
                )
                TableDetailsScreenState(table = table, userSession = userSession, isLoading = false)
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

    fun refreshData() {
        val currentTable = state.value.currentTable
        if (currentTable != null) {
            val tableId = currentTable.id
            if (tableId != null) {
                setupDetailsById(tableId)
            }
        }
    }

    fun showPartialPaymentDialog() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(showPartialPaymentDialog = true))
        }
    }

    fun hidePartialPaymentDialog() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(showPartialPaymentDialog = false))
        }
    }

    fun createPartialPayment(tableId: Int, paidBy: String, amountInCentavos: Long, description: String? = null, onSuccess: () -> Unit) {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(isProcessingPayment = true))
            
            repository.createPartialPayment(tableId, paidBy, amountInCentavos, description).fold(
                onSuccess = { 
                    println("Partial payment created successfully for $paidBy")
                    // Reload table data to show updated status
                    refreshData()
                    mutableState.emit(
                        state.value.copy(
                            isProcessingPayment = false,
                            showPartialPaymentDialog = false
                        )
                    )
                    onSuccess()
                },
                onFailure = { error ->
                    println("Error creating partial payment: ${error.message}")
                    mutableState.emit(
                        state.value.copy(
                            isProcessingPayment = false,
                            error = error as? co.kandalabs.comandaai.core.error.ComandaAiException
                        )
                    )
                }
            )
        }
    }

    fun reopenTable(table: Table) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            safeRunCatching {
                repository.reopenTable(tableId)
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

    fun showCloseTableConfirmation() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(showCloseTableConfirmation = true))
        }
    }

    fun hideCloseTableConfirmation() {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(showCloseTableConfirmation = false))
        }
    }
}
