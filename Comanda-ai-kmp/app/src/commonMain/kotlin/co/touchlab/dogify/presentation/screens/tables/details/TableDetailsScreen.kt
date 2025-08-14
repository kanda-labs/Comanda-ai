package co.touchlab.dogify.presentation.screens.tables.details

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.dogify.presentation.screens.order.OrderScreen
import co.touchlab.dogify.components.DogifyButton
import co.touchlab.dogify.components.DogifyButtonVariant
import co.touchlab.dogify.components.DogifyTopAppBar
import co.touchlab.dogify.theme.DogifyTypography
import co.touchlab.dogify.presentation.screens.itemsSelection.components.ErrorView
import co.touchlab.dogify.presentation.screens.itemsSelection.components.LoadingView
import co.touchlab.dogify.tokens.ComandaAiSpacing
import co.touchlab.dogify.tokens.ComandaAiColors
import kandalabs.commander.domain.model.Table
import kandalabs.commander.domain.model.TableStatus
import org.jetbrains.compose.ui.tooling.preview.Preview
import co.touchlab.dogify.presentation.screens.tables.details.component.TableDetailsOrders
import co.touchlab.dogify.presentation.designSystem.components.CommandaBadge
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kotlinx.datetime.LocalDateTime

public data class TableDetailsScreen(val table: Table) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<TablesDetailsViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        val actions: (TableDetailsAction) -> Unit = { action ->
            when (action) {
                TableDetailsAction.OPEN_TABLE -> viewModel.openTable(table)
                TableDetailsAction.CLOSE_TABLE -> viewModel.closeTable(table)
                TableDetailsAction.MAKE_ORDER -> {
                    val billId = table.billId
                    if (billId != null) {
                        navigator.push(
                            OrderScreen(
                                tableId = table.id ?: 0,
                                tableNumber = table.number.toString(),
                                billId = billId
                            )
                        )
                    } else {
                        // TODO: Mostrar erro - mesa sem bill ativa
                        println("Erro: Mesa ${table.number} não possui bill ativa")
                    }
                }
                TableDetailsAction.BACK -> navigator.pop()
            }

        }
        LaunchedEffect(Unit) {
            viewModel.setupDetails(table = table)
        }

        TableDetailsScreenContent(
            state = state,
            action = { action -> actions(action) }
        )
    }
}

@Composable
private fun TableDetailsScreenContent(
    state: TableDetailsScreenState,
    action: (TableDetailsAction) -> Unit
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
                    DogifyTopAppBar(
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
                            style = DogifyTypography.labelLarge,
                            modifier = Modifier.weight(2f)
                        )

                        state.badge?.let {
                            CommandaBadge(
                                text = it.text,
                                containerColor = it.color.value,
                                contentColor = it.textColor.value,
                                modifier = Modifier
                                    .padding(start = ComandaAiSpacing.Small.value)
                                    .weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                    Text(
                        "Pedidos",
                        modifier = Modifier
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        color = ComandaAiColors.Gray700.value,
                        style = DogifyTypography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                    TableDetailsOrders(
                        orders = state.orders.ordersPresentation,
                        onOrderClick = { /* ação ao clicar no pedido, se necessário */ },
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
            DogifyButton(
                text = it.text,
                variant = DogifyButtonVariant.Secondary,
                onClick = { action(it.action) },
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )
        }

        state.primaryButton?.let {
            DogifyButton(
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
                                items = emptyList(),
                                status = OrderStatus.OPEN,
                                createdAt = LocalDateTime(2025, 1, 28, 20, 0, 0),
                            )
                        )
                    ),
                isLoading = false,
                error = null
            ),
            action = {}
        )
    }
}
