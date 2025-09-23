package kandalabs.commander.presentation.models.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateBillRequest(
    val tableId: Int?,
    val tableNumber: Int?
)

@Serializable
data class UpdateBillRequest(
    val status: String,
    val finalizedByUserId: Int? = null
)

@Serializable
data class ProcessTablePaymentRequest(
    val finalizedByUserId: Int
)
