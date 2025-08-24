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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import kandalabs.commander.domain.model.Table
import org.jetbrains.compose.ui.tooling.preview.Preview

public data class PaymentSummaryScreen(val tableId: Int, val tableNumber: Int) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<PaymentSummaryViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        val actions: (PaymentSummaryAction) -> Unit = { action ->
            when (action) {
                PaymentSummaryAction.BACK -> navigator.pop()
                PaymentSummaryAction.FINISH_PAYMENT -> {
                    viewModel.finishPaymentById(tableId, {
                        navigator.pop()
                        navigator.pop()
                    })
//                    if (state.error == null && !state.isProcessingPayment) {
//                        navigator.pop()
//                        navigator.pop()
//                    }
                }

                PaymentSummaryAction.RETRY -> {
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
                    }
                    PaymentSummaryBottom(state = state, action = action)
                }
            }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total da conta:",
                    style = ComandaAiTypography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = state.totalAmountPresentation,
                    style = ComandaAiTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Primary.value
                )
            }

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            ComandaAiButton(
                text = "Finalizar pagamento",
                onClick = { action(PaymentSummaryAction.FINISH_PAYMENT) },
                isEnabled = !state.isProcessingPayment,
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )

            ComandaAiButton(
                text = "Voltar",
                variant = ComandaAiButtonVariant.Secondary,
                onClick = { action(PaymentSummaryAction.BACK) }
            )
        }
    }
}
