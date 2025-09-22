package co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails

import kotlinx.serialization.Serializable

data class PartialPaymentDetailsScreenState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val showCancelConfirmation: Boolean = false,
    val payment: PartialPaymentDetails? = null,
    val error: Throwable? = null
)

@Serializable
data class PartialPaymentDetails(
    val id: Int,
    val tableId: Int,
    val paidBy: String,
    val amountInCentavos: Long,
    val amountFormatted: String,
    val description: String?,
    val paymentMethod: String?,
    val receivedBy: String?,
    val status: String,
    val createdAt: String
)