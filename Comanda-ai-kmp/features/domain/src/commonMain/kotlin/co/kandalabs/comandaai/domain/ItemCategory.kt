package co.kandalabs.comandaai.domain

import kotlinx.serialization.Serializable

@Serializable
enum class ItemCategory { 
    SKEWER, 
    DRINK, 
    SNACK, 
    PROMOTIONAL 
}