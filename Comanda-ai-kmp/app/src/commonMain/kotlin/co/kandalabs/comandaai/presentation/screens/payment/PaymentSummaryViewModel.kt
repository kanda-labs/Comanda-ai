package co.kandalabs.comandaai.presentation.screens.payment

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.getOrThrow
import co.kandalabs.comandaai.domain.repository.TablesRepository
import kotlinx.coroutines.launch

internal class PaymentSummaryViewModel(
    private val repository: TablesRepository
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

    fun createPartialPayment(tableId: Int, paidBy: String, amountInCentavos: Long, description: String? = null, onSuccess: () -> Unit) {
        screenModelScope.launch {
            updateState { it.copy(isProcessingPayment = true) }
            
            repository.createPartialPayment(tableId, paidBy, amountInCentavos, description).fold(
                onSuccess = { 
                    println("Partial payment created successfully for $paidBy")
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