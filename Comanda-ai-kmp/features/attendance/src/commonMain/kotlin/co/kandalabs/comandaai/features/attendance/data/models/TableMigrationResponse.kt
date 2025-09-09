package co.kandalabs.comandaai.features.attendance.data.models

import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import kotlinx.serialization.Serializable

@Serializable
data class TableMigrationResponse(
    val success: Boolean,
    val message: String,
    val originTable: Table,
    val destinationTable: Table
)