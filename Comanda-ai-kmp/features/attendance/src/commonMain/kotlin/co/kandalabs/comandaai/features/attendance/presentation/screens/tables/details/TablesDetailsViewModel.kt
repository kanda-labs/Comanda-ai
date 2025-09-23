package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.sdk.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.sdk.session.SessionManager
import kotlinx.coroutines.launch

internal class TablesDetailsViewModel(
    private val repository: TablesRepository,
    private val sessionManager: SessionManager,
) : StateScreenModel<TableDetailsScreenState>(TableDetailsScreenState()) {


    fun openTable(table: Table) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch

            // Prevent double-clicking by checking if we're already processing
            if (state.value.isLoading) {
                return@launch
            }

            // Set loading state to prevent multiple clicks
            mutableState.emit(state.value.copy(isLoading = true))

            safeRunCatching {
                repository.openTable(tableId, table.number)
            }.fold(
                onSuccess = {
                    // Fetch updated table data from server to reflect status change
                    setupDetailsById(tableId)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(
                            isLoading = false,
                            error = error
                        )
                    )
                }
            )
        }
    }


    fun closeTable(table: Table, navigateToPayments: () -> Unit) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            val billId = table.billId ?: return@launch
            safeRunCatching {
                repository.closeTable(tableId, billId)
            }.fold(
                onSuccess = { updatedTable ->
                    if (sessionManager.getSession()?.role == UserRole.MANAGER) {
                        navigateToPayments()
                    } else {
                        val userSession = sessionManager.getSession()
                        mutableState.emit(
                            TableDetailsScreenState(
                                table = updatedTable,
                                userSession = userSession,
                                isLoading = false,
                                showCloseTableConfirmation = false
                            )
                        )
                    }
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(
                            error = error,
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

    fun reopenTable(table: Table) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            val billId = table.billId ?: return@launch
            safeRunCatching {
                repository.reopenTable(tableId, billId)
            }.fold(
                onSuccess = { updatedTable ->
                    // Use returned data directly instead of making new API call
                    val userSession = sessionManager.getSession()
                    mutableState.emit(
                        TableDetailsScreenState(
                            table = updatedTable,
                            userSession = userSession,
                            isLoading = false
                        )
                    )
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(error = error)
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
