package co.kandalabs.comandaai.features.attendance.domain.models.request

import kotlinx.serialization.Serializable

@Serializable
data class ProcessTablePaymentRequest(
    val finalizedByUserId: Int
)