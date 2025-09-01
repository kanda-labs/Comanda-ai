package co.kandalabs.comandaai.data.api

import co.kandalabs.comandaai.domain.models.model.Table
import kotlinx.serialization.Serializable

@Serializable
data class MigrationResponse(
    val success: Boolean,
    val message: String,
    val originTable: Table,
    val destinationTable: Table
)