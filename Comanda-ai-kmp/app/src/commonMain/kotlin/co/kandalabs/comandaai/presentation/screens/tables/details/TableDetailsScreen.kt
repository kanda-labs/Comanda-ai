package co.kandalabs.comandaai.presentation.screens.tables.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
import co.kandalabs.comandaai.utils.CurrencyFormatter
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
                TableDetailsAction.SHOW_PARTIAL_PAYMENT_DIALOG -> {
                    viewModel.showPartialPaymentDialog()
                }
                TableDetailsAction.HIDE_PARTIAL_PAYMENT_DIALOG -> {
                    viewModel.hidePartialPaymentDialog()
                }
                is TableDetailsAction.CREATE_PARTIAL_PAYMENT -> {
                    val currentTable = state.currentTable
                    if (currentTable?.id != null) {
                        viewModel.createPartialPayment(currentTable.id, action.paidBy, action.amountInCentavos, action.description) {
                            // Payment success callback
                            println("Partial payment created successfully for ${action.paidBy}")
                        }
                    }
                }
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
            
            // Partial Payment Dialog
            if (state.showPartialPaymentDialog) {
                PartialPaymentDialog(
                    onDismiss = { action(TableDetailsAction.HIDE_PARTIAL_PAYMENT_DIALOG) },
                    onConfirm = { paidBy, amount, description ->
                        action(TableDetailsAction.CREATE_PARTIAL_PAYMENT(paidBy, amount, description))
                    }
                )
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
        state.tertiaryButton?.let {
            ComandaAiButton(
                text = it.text,
                variant = ComandaAiButtonVariant.Secondary,
                onClick = { action(it.action) },
                isEnabled = !state.isProcessingPayment,
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )
        }
        
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

@Composable
private fun PartialPaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (paidBy: String, amountInCentavos: Long, description: String?) -> Unit
) {
    var paidBy by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(ComandaAiSpacing.Large.value),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ComandaAiSpacing.Large.value),
                verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value)
            ) {
                Text(
                    text = "Adicionar Pagamento Parcial",
                    style = ComandaAiTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("Quem está pagando?") },
                    placeholder = { Text("Ex: João, Mesa 5, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )


                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.text.filter { it.isDigit() }
                        if (digitsOnly.length <= 8) { // Limite: 999999.99
                            val formatted = if (digitsOnly.isEmpty()) {
                                ""
                            } else {
                                CurrencyFormatter.formatToBRL(digitsOnly)
                            }

                            amountText = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length) // mantém cursor no final
                            )
                        }
                    },
                    label = { Text("Valor") },
                    placeholder = { Text("R$ 0,00") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Ex: Pagamento via PIX") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value)
                ) {
                    ComandaAiButton(
                        text = "Cancelar",
                        variant = ComandaAiButtonVariant.Secondary,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ComandaAiButton(
                        text = "Confirmar",
                        onClick = {
                            if (paidBy.isNotBlank() && amountText.text.isNotBlank()) {
                                val amountInCentavos = CurrencyFormatter.brlToCentavos(amountText.text)
                                onConfirm(
                                    paidBy.trim(), 
                                    amountInCentavos, 
                                    description.trim().takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        isEnabled = paidBy.isNotBlank() && amountText.text.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private fun parseAmountToCentavos(amountText: String): Long {
    return CurrencyFormatter.brlToCentavos(amountText)
}
