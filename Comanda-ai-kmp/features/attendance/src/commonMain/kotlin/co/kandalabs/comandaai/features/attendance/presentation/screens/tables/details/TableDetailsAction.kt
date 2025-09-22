package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details

import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod

sealed class TableDetailsAction {
    object OPEN_TABLE : TableDetailsAction()
    object CLOSE_TABLE : TableDetailsAction()
    object MAKE_ORDER : TableDetailsAction()
    object BACK : TableDetailsAction()
    object SHOW_ORDER_DETAILS : TableDetailsAction()
    object CLOSE_TABLE_MANAGER : TableDetailsAction()
    object SHOW_PARTIAL_PAYMENT_DIALOG : TableDetailsAction()
    object HIDE_PARTIAL_PAYMENT_DIALOG : TableDetailsAction()

    data class CREATE_PARTIAL_PAYMENT(
        val paidBy: String,
        val amountInCentavos: Long,
        val description: String?,
        val paymentMethod: PaymentMethod?
    ) : TableDetailsAction()
    
    object REOPEN_TABLE : TableDetailsAction()
    object SHOW_CLOSE_TABLE_CONFIRMATION : TableDetailsAction()
    object HIDE_CLOSE_TABLE_CONFIRMATION : TableDetailsAction()
    object CONFIRM_CLOSE_TABLE : TableDetailsAction()
    object SHOW_TABLE_MIGRATION : TableDetailsAction()
}