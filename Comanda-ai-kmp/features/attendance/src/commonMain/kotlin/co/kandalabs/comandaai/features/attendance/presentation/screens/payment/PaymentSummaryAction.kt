package co.kandalabs.comandaai.features.attendance.presentation.screens.payment

import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod

sealed class PaymentSummaryAction {
    object BACK : PaymentSummaryAction()
    object FINISH_PAYMENT : PaymentSummaryAction()
    object SHOW_FINISH_PAYMENT_CONFIRMATION : PaymentSummaryAction()
    object HIDE_FINISH_PAYMENT_CONFIRMATION : PaymentSummaryAction()
    object RETRY : PaymentSummaryAction()
    object SHOW_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    object HIDE_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    data class CREATE_PARTIAL_PAYMENT(
        val paidBy: String?,
        val amountInCentavos: Long,
        val description: String? = null,
        val paymentMethod: PaymentMethod? = null
    ) : PaymentSummaryAction()
    data class NAVIGATE_TO_PARTIAL_PAYMENT_DETAILS(
        val paymentId: Int,
        val tableId: Int
    ) : PaymentSummaryAction()
}

