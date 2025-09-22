package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import co.kandalabs.comandaai.sdk.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.sdk.session.SessionManager
import kotlinx.coroutines.launch

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
                TableDetailsScreenState(
                    table = updatedTable,
                    userSession = userSession,
                    isLoading = false
                )
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
                    refreshTableData(tableId)
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(
                            isLoading = false,
                            error = error as? co.kandalabs.comandaai.sdk.error.ComandaAiException
                        )
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
                        state.value.copy(
                            isLoading = false,
                            error = error as? co.kandalabs.comandaai.sdk.error.ComandaAiException
                        )
                    )
                }
            )
        }
    }

    fun closeTable(table: Table, navigateToPayments: () -> Unit) {
        screenModelScope.launch {
            val tableId = table.id ?: return@launch
            safeRunCatching {
                repository.closeTable(tableId)
            }.fold(
                onSuccess = {
                    refreshTableData(tableId)

                    if (sessionManager.getSession()?.role == UserRole.MANAGER) {
                        navigateToPayments()
                    } else {
                        mutableState.emit(state.value.copy(showCloseTableConfirmation = false))
                    }
                },
                onFailure = { error ->
                    mutableState.emit(
                        state.value.copy(
                            error = error as? co.kandalabs.comandaai.sdk.error.ComandaAiException,
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
                println("🔄 TableDetailsViewModel: Refreshing data for table $tableId")
                setupDetailsById(tableId)
            } else {
                println("⚠️ TableDetailsViewModel: Table ID is null, cannot refresh")
            }
        } else {
            println("⚠️ TableDetailsViewModel: Current table is null, cannot refresh")
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

    fun createPartialPayment(
        tableId: Int,
        paidBy: String,
        amountInCentavos: Long,
        description: String? = null,
        paymentMethod: PaymentMethod? = null,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            mutableState.emit(state.value.copy(isProcessingPayment = true))

            // Get waiter name from session - this will be the receivedBy
            val session = sessionManager.getSession()
            val receivedBy = session?.name ?: "Sistema"

            repository.createPartialPayment(
                tableId = tableId,
                paidBy = paidBy, // Who paid (customer name)
                amountInCentavos = amountInCentavos,
                description = description,
                paymentMethod = paymentMethod,
                receivedBy = receivedBy // Waiter who received the payment
            ).fold(
                onSuccess = {
                    println("Partial payment created successfully - paid by: $paidBy, received by: $receivedBy")
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
                            error = error as? co.kandalabs.comandaai.sdk.error.ComandaAiException
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
                        state.value.copy(error = error as? co.kandalabs.comandaai.sdk.error.ComandaAiException)
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
