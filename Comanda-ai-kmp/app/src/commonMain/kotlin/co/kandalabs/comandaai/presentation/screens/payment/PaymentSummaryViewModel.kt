package co.kandalabs.comandaai.presentation.screens.payment

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.core.coroutinesResult.safeRunCatching
import co.kandalabs.comandaai.core.getOrThrow
import co.kandalabs.comandaai.domain.repository.TablesRepository
import co.kandalabs.comandaai.domain.repository.ItemsRepository
import kandalabs.commander.domain.model.Item
import io.ktor.util.reflect.instanceOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class PaymentSummaryViewModel(
    private val repository: TablesRepository,
    private val itemsRepository: ItemsRepository
) : StateScreenModel<PaymentSummaryScreenState>(PaymentSummaryScreenState()) {

    fun setupPaymentSummaryById(tableId: Int, tableNumber: Int) {
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            println("Setting up payment summary for table $tableId")
            
            safeRunCatching {
                val bill = repository.getBillByTableId(tableId)
                
                var totalAmount = 0
                itemsRepository.getItems(null).fold(
                    onSuccess = { items ->
                        totalAmount = bill.orders.sumOf { order ->
                            order.items.sumOf { orderItem -> 
                                val foundItem = items.firstOrNull { it.id == orderItem.itemId }
                                val itemValueInReais = foundItem?.value ?: 0
                                orderItem.count * itemValueInReais
                            }
                        }
                    },
                    onFailure = {
                        // Se falhar ao buscar itens, usa cálculo simples
                        totalAmount = bill.orders.sumOf { order ->
                            order.items.sumOf { item -> item.count * 1 }
                        }
                    }
                )
                
                PaymentSummaryScreenState(
                    isLoading = false,
                    tableNumber = tableNumber.toString().padStart(2, '0'),
                    orders = bill.orders,
                    totalAmount = totalAmount.toLong()
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

    private suspend fun updateState(transform: (PaymentSummaryScreenState) -> PaymentSummaryScreenState) {
        mutableState.emit(transform(state.value))
    }
}