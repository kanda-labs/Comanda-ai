package co.kandalabs.comandaai.presentation.screens.payment



sealed class PaymentSummaryAction {
    object BACK : PaymentSummaryAction()
    object FINISH_PAYMENT : PaymentSummaryAction()
    object RETRY : PaymentSummaryAction()
    object SHOW_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    object HIDE_PARTIAL_PAYMENT_DIALOG : PaymentSummaryAction()
    data class CREATE_PARTIAL_PAYMENT(val paidBy: String, val amountInCentavos: Long, val description: String? = null) : PaymentSummaryAction()
}

