package co.kandalabs.comandaai.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ItemStatus { 
    PENDING,       // Pendente - item aguardando processamento
    DELIVERED,     // Entregue - item finalizado e entregue ao cliente
    CANCELED       // Cancelado - item cancelado
}