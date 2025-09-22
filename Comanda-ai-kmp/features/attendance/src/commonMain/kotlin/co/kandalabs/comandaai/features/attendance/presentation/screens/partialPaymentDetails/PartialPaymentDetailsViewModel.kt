package co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import kotlinx.coroutines.launch

internal class PartialPaymentDetailsViewModel(
    private val repository: TablesRepository
) : StateScreenModel<PartialPaymentDetailsScreenState>(PartialPaymentDetailsScreenState()) {

    fun loadPaymentDetails(paymentId: Int, tableId: Int) {
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            repository.getPartialPaymentDetails(paymentId).fold(
                onSuccess = { payment ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            payment = payment
                        )
                    }
                },
                onFailure = { error ->
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

    fun showCancelConfirmation() {
        screenModelScope.launch {
            updateState { it.copy(showCancelConfirmation = true) }
        }
    }

    fun hideCancelConfirmation() {
        screenModelScope.launch {
            updateState { it.copy(showCancelConfirmation = false) }
        }
    }

    fun cancelPayment(paymentId: Int, onSuccess: () -> Unit) {
        screenModelScope.launch {
            updateState { it.copy(isProcessing = true, showCancelConfirmation = false) }

            repository.cancelPartialPayment(paymentId).fold(
                onSuccess = {
                    updateState { it.copy(isProcessing = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    updateState {
                        it.copy(
                            isProcessing = false,
                            error = error
                        )
                    }
                }
            )
        }
    }

    private suspend fun updateState(transform: (PartialPaymentDetailsScreenState) -> PartialPaymentDetailsScreenState) {
        mutableState.emit(transform(state.value))
    }
}