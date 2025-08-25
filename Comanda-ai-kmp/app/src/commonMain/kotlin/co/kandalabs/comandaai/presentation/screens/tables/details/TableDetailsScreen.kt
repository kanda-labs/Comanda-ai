package co.kandalabs.comandaai.presentation.screens.tables.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.presentation.screens.order.OrderScreen
import co.kandalabs.comandaai.presentation.screens.ordercontrol.OrderControlScreen
import co.kandalabs.comandaai.presentation.screens.payment.PaymentSummaryScreen
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.LoadingView
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.domain.models.model.Table
import co.kandalabs.comandaai.domain.models.model.TableStatus
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.kandalabs.comandaai.presentation.screens.tables.details.component.TableDetailsOrders
import co.kandalabs.comandaai.presentation.designSystem.components.CommandaBadge
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.models.model.OrderStatus
import kotlinx.datetime.LocalDateTime

public data class TableDetailsScreen(val tableId: Int, val tableNumber: Int) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<TablesDetailsViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        val actions: (TableDetailsAction) -> Unit = { action ->
            when (action) {
                TableDetailsAction.OPEN_TABLE -> {
                    val currentTable = state.currentTable
                    if (currentTable != null) {
                        viewModel.openTable(currentTable)
                    }
                }
                TableDetailsAction.CLOSE_TABLE -> {
                    val currentTable = state.currentTable
                    if (currentTable != null) {
                        viewModel.closeTable(currentTable)
                    }
                }
                TableDetailsAction.CLOSE_TABLE_MANAGER -> {
                    val currentTable = state.currentTable
                    if (currentTable != null) {
                        navigator.push(PaymentSummaryScreen(currentTable.id ?: 0, currentTable.number))
                    }
                }
                TableDetailsAction.MAKE_ORDER -> {
                    val currentTable = state.currentTable
                    val billId = currentTable?.billId
                    if (billId != null) {
                        navigator.push(
                            OrderScreen(
                                tableId = currentTable.id ?: 0,
                                tableNumber = currentTable.number.toString(),
                                billId = billId
                            )
                        )
                    } else {
                        // TODO: Mostrar erro - mesa sem bill ativa
                        println("Erro: Mesa ${currentTable?.number ?: tableNumber} não possui bill ativa")
                    }
                }
                TableDetailsAction.BACK -> navigator.pop()
                TableDetailsAction.SHOW_ORDER_DETAILS -> { /* Handled by onOrderClick */ }
            }

        }
        LaunchedEffect(Unit) {
            viewModel.setupDetailsById(tableId = tableId)
        }
        
        LaunchedEffect(navigator.size) {
            if (navigator.size == 1 && navigator.lastItem == this@TableDetailsScreen) {
                viewModel.refreshData()
            }
        }

        TableDetailsScreenContent(
            state = state,
            action = { action -> actions(action) },
            onOrderClick = { order -> 
                order.id?.let { orderId ->
                    navigator.push(OrderControlScreen(orderId))
                }
            }
        )
    }
}

@Composable
private fun TableDetailsScreenContent(
    state: TableDetailsScreenState,
    action: (TableDetailsAction) -> Unit,
    onOrderClick: (Order) -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (state.isLoading) {
                LoadingView()
            } else if (state.error != null) {
                ErrorView(
                    error = state.error,
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    ComandaAiTopAppBar(
                        state.appBarTitle,
                        onBackOrClose = { action(TableDetailsAction.BACK) },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    )

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = ComandaAiSpacing.Medium.value)
                            .fillMaxSize()
                            .weight(0.1f)
                    ) {
                        Text(
                            text = state.contentTitle,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = ComandaAiTypography.labelLarge,
                            modifier = Modifier.weight(2f)
                        )

                        state.badge?.let {
                            CommandaBadge(
                                text = it.text,
                                containerColor = it.color.value,
                                contentColor = it.textColor.value,
                                modifier = Modifier
                                    .padding(start = ComandaAiSpacing.Small.value)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                    Text(
                        "Pedidos",
                        modifier = Modifier
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        color = ComandaAiColors.Gray700.value,
                        style = ComandaAiTypography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                    TableDetailsOrders(
                        orders = state.orders.ordersPresentation,
                        onOrderClick = onOrderClick,
                        modifier = Modifier
                            .weight(1f)
                    )

                    TableDetailsButtons(state, action)
                }
            }
        }
    }
}

@Composable
private fun TableDetailsButtons(
    state: TableDetailsScreenState,
    action: (TableDetailsAction) -> Unit
) {
    Column(modifier = Modifier.padding(ComandaAiSpacing.Large.value)) {
        state.secondaryButton?.let {
            ComandaAiButton(
                text = it.text,
                variant = ComandaAiButtonVariant.Secondary,
                onClick = { action(it.action) },
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )
        }

        state.primaryButton?.let {
            ComandaAiButton(
                text = it.text,
                onClick = { action(it.action) }
            )
        }
    }
}


@Preview
@Composable
private fun TableDetailsScreenPreview() {
    MaterialTheme {
        TableDetailsScreenContent(
            state = TableDetailsScreenState(
                table =
                    Table(
                        number = 1,
                        status = TableStatus.OCCUPIED,
                        orders = listOf(
                            Order(
                                id = 1,
                                billId = 1,
                                tableNumber = 1,
                                userName = "usuario-teste",
                                items = emptyList(),
                                status = OrderStatus.PENDING,
                                createdAt = LocalDateTime(2025, 1, 28, 20, 0, 0),
                            )
                        )
                    ),
                isLoading = false,
                error = null
            ),
            action = {},
            onOrderClick = {}
        )
    }
}
