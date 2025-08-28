package co.kandalabs.comandaai.presentation.screens.payment



sealed class PaymentSummaryAction {
    object BACK : PaymentSummaryAction()
    object FINISH_PAYMENT : PaymentSummaryAction()
    object SHOW_FINISH_PAYMENT_CONFIRMATION : PaymentSummaryAction()
    object HIDE_FINISH_PAYMENT_CONFIRMATION : PaymentSummaryAction()
    object RETRY : PaymentSummaryAction()
    object SHOW_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    object HIDE_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    data class CREATE_PARTIAL_PAYMENT(val paidBy: String, val amountInCentavos: Long, val description: String? = null) : PaymentSummaryAction()
}

