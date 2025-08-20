package co.kandalabs.comandaai.presentation.screens.payment

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.getOrThrow
import co.kandalabs.comandaai.domain.repository.TablesRepository
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class PaymentSummaryViewModel(
    private val repository: TablesRepository
) : StateScreenModel<PaymentSummaryScreenState>(PaymentSummaryScreenState()) {

    private companion object {
        const val MAX_RETRIES = 3
        const val RETRY_DELAY_MS = 100L
    }

    fun setupPaymentSummaryById(tableId: Int, tableNumber: Int) {
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            println("Setting up payment summary for table $tableId")
            
            safeRunCatching {
                val bill = repository.getBillByTableId(tableId)
                val totalAmount = bill.orders.sumOf { order ->
                    order.items.sumOf { item -> item.count * 1.0 }
                }
                PaymentSummaryScreenState(
                    isLoading = false,
                    tableNumber = tableNumber.toString().padStart(2, '0'),
                    orders = bill.orders,
                    totalAmount = totalAmount
                )
            }.fold(
                onSuccess = { newState -> 
                    println("Payment summary loaded successfully")
                    updateState { newState }
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
            val currentState = state.value
            updateState { it.copy(isProcessingPayment = true) }
            safeRunCatching {
                val bill = repository.getBillByTableId(tableId)
                val billId = bill.id.getOrThrow()
                repository.finishTablePayment(tableId, billId, currentState.totalAmount)
                    .getOrThrow()
            }.fold(
                onSuccess = { updateState {
                    onFinish()
                    it.copy(isProcessingPayment = false)
                } },
                onFailure = { error ->
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

    private suspend fun retryOnCancellation(block: suspend () -> PaymentSummaryScreenState) {
        var attempts = 0
        while (attempts < MAX_RETRIES) {
            val result = safeRunCatching { block() }
            result.fold(
                onSuccess = { state -> return updateState { state } },
                onFailure = { error ->
                    println("Attempt $attempts failed: ${error.message}")
                    if (error is kotlinx.coroutines.CancellationException && attempts < MAX_RETRIES - 1) {
                        attempts++
                        println("Retrying after cancellation, attempt ${attempts + 1}")
                        delay(RETRY_DELAY_MS)
                    } else {
                        return updateState {
                            it.copy(
                                isLoading = false,
                                error = error
                            )
                        }
                    }
                }
            )
        }
    }

    private suspend fun updateState(transform: (PaymentSummaryScreenState) -> PaymentSummaryScreenState) {
        mutableState.emit(transform(state.value))
    }
}