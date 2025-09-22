package co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails

sealed class PartialPaymentDetailsAction {
    object BACK : PartialPaymentDetailsAction()
    object SHOW_CANCEL_CONFIRMATION : PartialPaymentDetailsAction()
    object HIDE_CANCEL_CONFIRMATION : PartialPaymentDetailsAction()
    object CONFIRM_CANCEL : PartialPaymentDetailsAction()
}