package co.kandalabs.comandaai.features.attendance.domain.models.enum

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentMethod(val displayName: String) {
    PIX("PIX"),
    CARTAO("Cartão"),
    DINHEIRO("Dinheiro")
}