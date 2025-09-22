package co.kandalabs.comandaai.features.attendance.presentation.screens.payment

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import co.kandalabs.comandaai.sdk.session.SessionManager
import kotlinx.coroutines.launch

internal class PaymentSummaryViewModel(
    private val repository: TablesRepository,
    private val sessionManager: SessionManager
) : StateScreenModel<PaymentSummaryScreenState>(PaymentSummaryScreenState()) {

    fun setupPaymentSummaryById(tableId: Int, tableNumber: Int) {
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            println("Setting up payment summary for table $tableId")
            
            repository.getPaymentSummary(tableId).fold(
                onSuccess = { paymentSummary -> 
                    println("Payment summary loaded successfully")
                    updateState { 
                        PaymentSummaryScreenState(
                            isLoading = false,
                            tableNumber = paymentSummary.tableNumber,
                            paymentSummary = paymentSummary
                        )
                    }
                },
                onFailure = { error ->
                    println("Error loading payment summary: ${error.message}")
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            )
        }
    }

    fun finishPaymentById(tableId: Int, onFinish: () -> Unit) {
        screenModelScope.launch {
            updateState { it.copy(isProcessingPayment = true) }
            
            repository.processTablePayment(tableId).fold(
                onSuccess = { 
                    println("Payment processed successfully for table $tableId")
                    updateState {
                        onFinish()
                        it.copy(
                            isProcessingPayment = false,
                            showFinishPaymentConfirmation = false
                        )
                    }
                },
                onFailure = { error ->
                    println("Error processing payment: ${error.message}")
                    updateState {
                        it.copy(
                            isProcessingPayment = false,
                            showFinishPaymentConfirmation = false,
                            error = error
                        )
                    }
                }
            )
        }
    }

    fun createPartialPayment(
        tableId: Int,
        paidBy: String?,
        amountInCentavos: Long,
        description: String? = null,
        paymentMethod: PaymentMethod? = null,
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            updateState { it.copy(isProcessingPayment = true) }

            // Get waiter name from session - this will be the receivedBy
            val session = sessionManager.getSession()
            val receivedBy = session?.name ?: "Sistema"

            repository.createPartialPayment(
                tableId = tableId,
                paidBy = paidBy ?: "Cliente", // Who paid (customer name or "Cliente" if empty)
                amountInCentavos = amountInCentavos,
                description = description,
                paymentMethod = paymentMethod,
                receivedBy = receivedBy // Waiter who received the payment
            ).fold(
                onSuccess = {
                    println("Partial payment created successfully - paid by: ${paidBy ?: "Cliente"}, received by: $receivedBy")
                    // Reload payment summary to show updated statuses
                    setupPaymentSummaryById(tableId, state.value.tableNumber.toIntOrNull() ?: 0)
                    updateState {
                        it.copy(
                            isProcessingPayment = false,
                            showPartialPaymentDialog = false
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    println("Error creating partial payment: ${error.message}")
                    updateState {
                        it.copy(
                            isProcessingPayment = false,
                            error = error
                        )
                    }
                }
            )
        }
    }

    fun showPartialPaymentDialog() {
        screenModelScope.launch {
            updateState { it.copy(showPartialPaymentDialog = true) }
        }
    }

    fun hidePartialPaymentDialog() {
        screenModelScope.launch {
            updateState { it.copy(showPartialPaymentDialog = false) }
        }
    }

    fun showFinishPaymentConfirmation() {
        screenModelScope.launch {
            updateState { it.copy(showFinishPaymentConfirmation = true) }
        }
    }

    fun hideFinishPaymentConfirmation() {
        screenModelScope.launch {
            updateState { it.copy(showFinishPaymentConfirmation = false) }
        }
    }

    private suspend fun updateState(transform: (PaymentSummaryScreenState) -> PaymentSummaryScreenState) {
        mutableState.emit(transform(state.value))
    }
}