package co.kandalabs.comandaai.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ItemStatus { 
    GRANTED,       // Concluído (mantém compatibilidade)
    OPEN,          // Pendente (mantém compatibilidade)
    CANCELED,      // Cancelado (mantém compatibilidade)
    IN_PRODUCTION, // Em produção (novo)
    COMPLETED,     // Finalizado (novo)
    DELIVERED      // Entregue (novo)
}