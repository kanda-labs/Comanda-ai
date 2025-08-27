package co.kandalabs.comandaai.presentation.screens.tables.details

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
        val description: String?
    ) : TableDetailsAction()
}