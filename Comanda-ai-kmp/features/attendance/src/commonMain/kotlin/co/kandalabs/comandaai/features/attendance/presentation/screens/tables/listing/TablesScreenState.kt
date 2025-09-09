package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing

import androidx.compose.ui.graphics.Color
import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.models.model.TableStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class TablesScreenState(
    private val tables: ImmutableList<Table> = persistentListOf(),
    val title: String = "Mesas",
    val tablesPresentation: List<TablePresentation> = tables.map { TablePresentation(it) },
    val isLoading: Boolean = false,
    val error: ComandaAiException? = null
)

internal data class TablePresentation(
    val table: Table
) {
    val number: String = buildString { if (table.number < 10) append("0") }.plus(table.number)
    val backGroundColor: ComandaAiColors = when (table.status) {
        TableStatus.OCCUPIED -> ComandaAiColors.Yellow500
        TableStatus.ON_PAYMENT -> ComandaAiColors.Secondary
        TableStatus.FREE -> ComandaAiColors.Green500
    }
    val textColor = when (backGroundColor) {
        ComandaAiColors.Yellow500 -> Color.Black
        ComandaAiColors.Secondary -> Color.Black
        ComandaAiColors.Green500 -> Color.White
        else -> Color.Black
    }

}
