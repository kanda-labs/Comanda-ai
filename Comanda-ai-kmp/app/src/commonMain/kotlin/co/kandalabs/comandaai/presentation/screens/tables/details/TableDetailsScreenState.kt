package co.kandalabs.comandaai.presentation.screens.tables.details

import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiColors
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus
import kotlinx.datetime.Month
import kotlinx.datetime.LocalDateTime

internal data class TableDetailsScreenState(
    private val table: Table? = null,
    val isLoading: Boolean = true,
    val error: ComandaAiException? = null
) {
    val currentTable: Table? get() = table
    val appBarTitle = "Detalhes da mesa"
    private val presentationNumber = buildString {
        if ((table?.number ?: 0) < 10) append("0")
    }.plus(table?.number)
    val contentTitle: String = "Mesa $presentationNumber"

    val badge: TableDetailsScreenBadge? = when (table?.status) {
        TableStatus.OCCUPIED -> TableDetailsScreenBadge(
            text = table.status.presentationName,
            color = ComandaAiColors.Yellow500,
            textColor = ComandaAiColors.OnSurface
        )

        TableStatus.ON_PAYMENT -> TableDetailsScreenBadge(
            text = table.status.presentationName,
            color = ComandaAiColors.Secondary,
            textColor = ComandaAiColors.OnSurface
        )

        TableStatus.FREE -> TableDetailsScreenBadge(
            text = table.status.presentationName,
            color = ComandaAiColors.Green500,
            textColor = ComandaAiColors.OnSurface
        )

        else -> null
    }
    val primaryButton: TableDetailsScreenButton? = when (table?.status) {
        TableStatus.OCCUPIED -> TableDetailsScreenButton(
            text = "Fazer pedido",
            action = TableDetailsAction.MAKE_ORDER
        )

        TableStatus.FREE -> TableDetailsScreenButton(
            text = "Abrir conta",
            action = TableDetailsAction.OPEN_TABLE
        )

        else -> null
    }

    val secondaryButton: TableDetailsScreenButton? = when (table?.status) {
        TableStatus.OCCUPIED -> TableDetailsScreenButton(
            text = "Fechar conta",
            action = TableDetailsAction.CLOSE_TABLE
        )

        TableStatus.FREE, TableStatus.ON_PAYMENT -> TableDetailsScreenButton(
            text = "Voltar",
            action = TableDetailsAction.BACK
        )

        else -> null
    }

    val orders: OrdersDetailsState = OrdersDetailsState(
        orders = table?.orders ?: emptyList(),
        isLoading = isLoading,
        error = error
    )

}

internal data class OrdersDetailsItemState(
    val id: String,
    val time: String,
    val status: TableDetailsScreenBadge,
    val order: Order
)

internal data class OrdersDetailsState(
    private val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: ComandaAiException? = null
){
    val ordersPresentation: List<OrdersDetailsItemState> = orders.map {
        val (color, textColor) = when (it.status) {
            kandalabs.commander.domain.model.OrderStatus.GRANTED -> Pair(
                ComandaAiColors.Green500,
                ComandaAiColors.OnSurface
            )
            kandalabs.commander.domain.model.OrderStatus.OPEN -> Pair(
                ComandaAiColors.Blue500,
                ComandaAiColors.OnSurface
            )
            kandalabs.commander.domain.model.OrderStatus.CANCELED -> Pair(
                ComandaAiColors.Error,
                ComandaAiColors.OnError
            )
        }

        OrdersDetailsItemState(
            id = "Nº ${it.id}",
            time = it.createdAt.formatPtBrDateTime(),
            status = TableDetailsScreenBadge(
                text = it.status.presentationName,
                color = color,
                textColor = textColor
            ),
            order = it
        )
    }
}

internal data class TableDetailsScreenBadge(
    val text: String,
    val color: ComandaAiColors,
    val textColor: ComandaAiColors = ComandaAiColors.OnSurface
)

internal data class TableDetailsScreenButton(
    val text: String,
    val action: TableDetailsAction
)

// Extension para formatar LocalDateTime no padrão "dd MMM • HH:mm" em PT-BR
fun LocalDateTime.formatPtBrDateTime(): String {
    val day = this.dayOfMonth.toString().padStart(2, '0')
    val month = when (this.month) {
        Month.JANUARY -> "Jan"
        Month.FEBRUARY -> "Fev"
        Month.MARCH -> "Mar"
        Month.APRIL -> "Abr"
        Month.MAY -> "Mai"
        Month.JUNE -> "Jun"
        Month.JULY -> "Jul"
        Month.AUGUST -> "Ago"
        Month.SEPTEMBER -> "Set"
        Month.OCTOBER -> "Out"
        Month.NOVEMBER -> "Nov"
        Month.DECEMBER -> "Dez"
        else -> ""
    }
    val hour = this.hour.toString().padStart(2, '0')
    val minute = this.minute.toString().padStart(2, '0')
    return "$day $month • $hour:$minute"
}
