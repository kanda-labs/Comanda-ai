package co.kandalabs.comandaai.presentation.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.LoadingView
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.domain.models.model.Table
import co.kandalabs.comandaai.utils.CurrencyFormatter
import org.jetbrains.compose.ui.tooling.preview.Preview

public data class PaymentSummaryScreen(val tableId: Int, val tableNumber: Int) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<PaymentSummaryViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val actions: (PaymentSummaryAction) -> Unit = { action ->
            when (action) {
                is PaymentSummaryAction.BACK -> navigator.pop()
                is PaymentSummaryAction.FINISH_PAYMENT -> {
                    viewModel.finishPaymentById(tableId, {
                        navigator.pop()
                        navigator.pop()
                    })
//                    if (state.error == null && !state.isProcessingPayment) {
//                        navigator.pop()
//                        navigator.pop()
//                    }
                }
                is PaymentSummaryAction.SHOW_PARTIAL_PAYMENT_DIALOG -> {
                    viewModel.showPartialPaymentDialog()
                }
                is PaymentSummaryAction.HIDE_PARTIAL_PAYMENT_DIALOG -> {
                    viewModel.hidePartialPaymentDialog()
                }
                is PaymentSummaryAction.CREATE_PARTIAL_PAYMENT -> {
                    viewModel.createPartialPayment(tableId, action.paidBy, action.amountInCentavos, action.description) {
                        // Payment success callback - could show a toast or update UI
                        println("Partial payment created successfully for ${action.paidBy}")
                    }
                }
                is PaymentSummaryAction.RETRY -> {
                    viewModel.setupPaymentSummaryById(tableId, tableNumber)
                }
            }
        }

        LaunchedEffect(tableId, tableNumber) {
            viewModel.setupPaymentSummaryById(tableId, tableNumber)
        }

        PaymentSummaryScreenContent(
            state = state,
            action = actions
        )
    }
}

@Composable
private fun PaymentSummaryScreenContent(
    state: PaymentSummaryScreenState,
    action: (PaymentSummaryAction) -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                state.isLoading -> LoadingView()
                state.error != null -> ErrorView(
                    error = state.error,
                    onRetry = { action(PaymentSummaryAction.RETRY) } // Add retry action
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    ComandaAiTopAppBar(
                        state.appBarTitle,
                        onBackOrClose = { action(PaymentSummaryAction.BACK) },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    )
                    // Rest of the UI remains unchanged
                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                    Text(
                        text = state.contentTitle,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = ComandaAiTypography.labelLarge,
                        modifier = Modifier.padding(horizontal = ComandaAiSpacing.Medium.value)
                    )
                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                    
                    // Seção de resumo de pagamento
                    PaymentSummarySection(state = state)
                    
                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                    
                    // Lista de itens da conta (mantém visualização atual)
                    Text(
                        "Itens da conta",
                        modifier = Modifier.padding(horizontal = ComandaAiSpacing.Medium.value),
                        color = ComandaAiColors.Gray700.value,
                        style = ComandaAiTypography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                    ) {
                        if (state.compiledItems.isEmpty()) {
                            item {
                                Text(
                                    text = "Nenhum item encontrado para esta mesa",
                                    style = ComandaAiTypography.bodyLarge,
                                    color = ComandaAiColors.Gray600.value,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(ComandaAiSpacing.Medium.value),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            items(state.compiledItems) { item ->
                                CompiledItemCard(item = item)
                            }
                        }
                        
                        // Histórico de pagamentos parciais
                        if (state.hasPartialPayments) {
                            item {
                                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                                Text(
                                    "Pagamentos realizados",
                                    style = ComandaAiTypography.titleMedium,
                                    color = ComandaAiColors.Gray700.value,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                            }
                            
                            items(state.partialPaymentsList) { payment ->
                                PartialPaymentCard(payment = payment)
                            }
                        }
                    }
                    PaymentSummaryBottom(state = state, action = action)
                }
            }
        }
        
        // Partial Payment Dialog
        if (state.showPartialPaymentDialog) {
            PartialPaymentDialog(
                onDismiss = { action(PaymentSummaryAction.HIDE_PARTIAL_PAYMENT_DIALOG) },
                onConfirm = { paidBy, amount, description ->
                    action(PaymentSummaryAction.CREATE_PARTIAL_PAYMENT(paidBy, amount, description))
                }
            )
        }
    }
}

@Composable
private fun CompiledItemCard(item: PaymentItemState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = item.quantityText,
                        style = ComandaAiTypography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = ComandaAiColors.Gray600.value
                    )
                    Text(
                        text = " ${item.name}",
                        style = ComandaAiTypography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                item.observation?.let { obs ->
                    if (obs.isNotBlank()) {
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                        Text(
                            text = "Obs: $obs",
                            style = ComandaAiTypography.bodySmall,
                            color = ComandaAiColors.Gray500.value
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedPrice,
                    style = ComandaAiTypography.bodyMedium,
                    color = ComandaAiColors.Gray600.value
                )
                Text(
                    text = item.formattedTotal,
                    style = ComandaAiTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Primary.value
                )
            }
        }
    }
}

@Composable
private fun PaymentSummarySection(
    state: PaymentSummaryScreenState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComandaAiSpacing.Medium.value),
        colors = CardDefaults.cardColors(containerColor = ComandaAiColors.Surface.value),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value)
        ) {
            // Total da conta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total da conta:",
                    style = ComandaAiTypography.bodyLarge,
                    color = ComandaAiColors.OnSurface.value
                )
                Text(
                    text = state.totalAmountPresentation,
                    style = ComandaAiTypography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiColors.OnSurface.value
                )
            }
            
            // Valor já pago (se houver)
            if (state.hasPartialPayments) {
                Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Já foi pago:",
                        style = ComandaAiTypography.bodyMedium,
                        color = ComandaAiColors.Green500.value
                    )
                    Text(
                        text = state.totalPaidPresentation,
                        style = ComandaAiTypography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = ComandaAiColors.Green500.value
                    )
                }
                
                Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                HorizontalDivider(
                    color = ComandaAiColors.Gray300.value,
                    modifier = Modifier.padding(vertical = ComandaAiSpacing.xSmall.value)
                )
                
                // Saldo restante
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Falta pagar:",
                        style = ComandaAiTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isFullyPaid) ComandaAiColors.Green500.value else ComandaAiColors.Primary.value
                    )
                    Text(
                        text = state.remainingAmountPresentation,
                        style = ComandaAiTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isFullyPaid) ComandaAiColors.Green500.value else ComandaAiColors.Primary.value
                    )
                }
            }
        }
    }
}

@Composable
private fun PartialPaymentCard(
    payment: PartialPaymentState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ComandaAiColors.Green50.value),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.displayDescription,
                    style = ComandaAiTypography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiColors.OnSurface.value
                )
                Text(
                    text = payment.timeAgo,
                    style = ComandaAiTypography.bodySmall,
                    color = ComandaAiColors.Gray500.value
                )
            }
            
            Text(
                text = payment.amount,
                style = ComandaAiTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ComandaAiColors.Green500.value
            )
        }
    }
}


@Composable
private fun PaymentSummaryBottom(
    state: PaymentSummaryScreenState,
    action: (PaymentSummaryAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(ComandaAiSpacing.Large.value)
        ) {
            HorizontalDivider(color = ComandaAiColors.Gray300.value)

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            // Mostrar status se a conta está totalmente paga
            if (state.isFullyPaid) {
                Text(
                    text = "✅ Conta totalmente paga!",
                    style = ComandaAiTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Green500.value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
            }
            
            // Mostrar resumo e opções de pagamento sempre
            if (state.hasPartialPayments && !state.isFullyPaid) {
                Text(
                    text = "Restante: ${state.remainingAmountPresentation}",
                    style = ComandaAiTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Primary.value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (!state.isFullyPaid) {
                Text(
                    text = "Total: ${state.totalAmountPresentation}",
                    style = ComandaAiTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Primary.value,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            if (!state.isFullyPaid) {
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
            }

            // Botão para finalizar pagamento - sempre mostrar
            val buttonText = if (state.isProcessingPayment) {
                "Processando..."
            } else if (state.isFullyPaid) {
                "Finalizar mesa"
            } else if (state.hasPartialPayments) {
                "Pagar restante"
            } else {
                "Finalizar pagamento"
            }

            ComandaAiButton(
                text = buttonText,
                onClick = { action(PaymentSummaryAction.FINISH_PAYMENT) },
                isEnabled = !state.isProcessingPayment,
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )

            // Mostrar botão de pagamento parcial apenas se não estiver totalmente pago
            if (!state.isFullyPaid) {
                ComandaAiButton(
                    text = "Adicionar Pagamento Parcial",
                    variant = ComandaAiButtonVariant.Secondary,
                    onClick = { action(PaymentSummaryAction.SHOW_PARTIAL_PAYMENT_DIALOG) },
                    isEnabled = !state.isProcessingPayment,
                    modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
                )
            }

            ComandaAiButton(
                text = "Voltar",
                variant = ComandaAiButtonVariant.Secondary,
                onClick = { action(PaymentSummaryAction.BACK) }
            )
        }
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
