package co.kandalabs.comandaai.features.attendance.domain.models.request

data class CreateOrderItemRequest(
    val itemId: Int,
    val name: String,
    val count: Int,
    val observation: String? = null
)