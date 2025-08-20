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
                        it.copy(isProcessingPayment = false)
                    }
                },
                onFailure = { error ->
                    println("Error processing payment: ${error.message}")
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

    private suspend fun updateState(transform: (PaymentSummaryScreenState) -> PaymentSummaryScreenState) {
        mutableState.emit(transform(state.value))
    }
}