package kandalabs.commander.presentation.models.response

import kotlinx.serialization.Serializable

@Serializable
data class MigrationResponse(
    val success: Boolean,
    val message: String,
    val originTable: TableDto,
    val destinationTable: TableDto
)