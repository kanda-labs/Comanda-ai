package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.kandalabs.comandaai.features.attendance.domain.repository.TablesRepository
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import co.kandalabs.comandaai.sdk.session.SessionManager
import kotlinx.coroutines.launch

internal class PaymentHistoryViewModel(
    private val repository: TablesRepository,
    private val sessionManager: SessionManager
) : StateScreenModel<PaymentHistoryScreenState>(PaymentHistoryScreenState()) {

    fun loadPaymentHistory() {
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }

            val session = sessionManager.getSession()
            val userId = session?.userId

            if (userId == null) {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = Exception("Usuário não encontrado")
                    )
                }
                return@launch
            }

            val currentState = state.value

            repository.getPaymentHistory(
                userId = userId,
                startDate = currentState.startDateWithHour,
                endDate = currentState.endDateWithHour,
                paymentMethod = currentState.appliedPaymentMethod
            ).fold(
                onSuccess = { payments ->
                    val totalAmount = payments.sumOf { it.amountInCentavos }
                    val totalAmountFormatted = formatCurrency(totalAmount)

                    updateState {
                        it.copy(
                            isLoading = false,
                            payments = payments,
                            totalAmount = totalAmount,
                            totalAmountFormatted = totalAmountFormatted,
                            error = null
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

    // Temporary filter setters (don't trigger reload)
    fun setTempConsiderPreviousDay(considerPreviousDay: Boolean) {
        screenModelScope.launch {
            updateState {
                it.copy(tempConsiderPreviousDay = considerPreviousDay)
            }
        }
    }

    fun setTempStartTime(hour: Int, minute: Int) {
        screenModelScope.launch {
            updateState {
                it.copy(tempStartHour = hour, tempStartMinute = minute)
            }
        }
    }

    fun setTempEndTime(hour: Int, minute: Int) {
        screenModelScope.launch {
            updateState {
                it.copy(tempEndHour = hour, tempEndMinute = minute)
            }
        }
    }

    fun setTempPaymentMethod(paymentMethod: PaymentMethod?) {
        screenModelScope.launch {
            updateState {
                it.copy(tempPaymentMethod = paymentMethod)
            }
        }
    }

    fun setFiltersExpanded(expanded: Boolean) {
        screenModelScope.launch {
            updateState {
                it.copy(isFiltersExpanded = expanded)
            }
        }
    }

    fun setShowStartTimePicker(show: Boolean) {
        screenModelScope.launch {
            updateState {
                it.copy(showStartTimePicker = show)
            }
        }
    }

    fun setShowEndTimePicker(show: Boolean) {
        screenModelScope.launch {
            updateState {
                it.copy(showEndTimePicker = show)
            }
        }
    }

    // Apply temporary filters to actual filters and reload
    fun applyFilters() {
        screenModelScope.launch {
            updateState { currentState ->
                currentState.copy(
                    appliedPaymentMethod = currentState.tempPaymentMethod,
                    appliedConsiderPreviousDay = currentState.tempConsiderPreviousDay,
                    appliedStartHour = currentState.tempStartHour,
                    appliedStartMinute = currentState.tempStartMinute,
                    appliedEndHour = currentState.tempEndHour,
                    appliedEndMinute = currentState.tempEndMinute,
                    isFiltersExpanded = false
                )
            }
        }
        loadPaymentHistory()
    }

    // Clear all filters
    fun clearFilters() {
        screenModelScope.launch {
            updateState {
                it.copy(
                    tempPaymentMethod = null,
                    tempConsiderPreviousDay = false,
                    tempStartHour = 0,
                    tempStartMinute = 0,
                    tempEndHour = 23,
                    tempEndMinute = 59,
                    appliedPaymentMethod = null,
                    appliedConsiderPreviousDay = false,
                    appliedStartHour = 0,
                    appliedStartMinute = 0,
                    appliedEndHour = 23,
                    appliedEndMinute = 59
                )
            }
        }
        loadPaymentHistory()
    }

    fun retry() {
        loadPaymentHistory()
    }

    private fun formatCurrency(amountInCentavos: Long): String {
        val reais = amountInCentavos / 100
        val centavos = amountInCentavos % 100
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }

    private suspend fun updateState(transform: (PaymentHistoryScreenState) -> PaymentHistoryScreenState) {
        mutableState.emit(transform(state.value))
    }
}