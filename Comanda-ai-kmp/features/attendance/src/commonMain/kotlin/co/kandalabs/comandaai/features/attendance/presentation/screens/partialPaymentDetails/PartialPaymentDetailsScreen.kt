package co.kandalabs.comandaai.features.attendance.presentation.screens.partialPaymentDetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.core.utils.DateTimeFormatter
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.sdk.error.ComandaAiException
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource

data class PartialPaymentDetailsScreen(
    val paymentId: Int,
    val tableId: Int
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<PartialPaymentDetailsViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            viewModel.loadPaymentDetails(paymentId, tableId)
        }

        val actions: (PartialPaymentDetailsAction) -> Unit = { action ->
            when (action) {
                PartialPaymentDetailsAction.BACK -> navigator.pop()
                PartialPaymentDetailsAction.SHOW_CANCEL_CONFIRMATION -> {
                    viewModel.showCancelConfirmation()
                }

                PartialPaymentDetailsAction.HIDE_CANCEL_CONFIRMATION -> {
                    viewModel.hideCancelConfirmation()
                }

                PartialPaymentDetailsAction.CONFIRM_CANCEL -> {
                    viewModel.cancelPayment(paymentId) {
                        navigator.pop()
                    }
                }
            }
        }

        PartialPaymentDetailsScreenContent(
            state = state,
            action = actions
        )
    }
}

@Composable
private fun PartialPaymentDetailsScreenContent(
    state: PartialPaymentDetailsScreenState,
    action: (PartialPaymentDetailsAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ComandaAiTheme.colorScheme.background
    ) {
            if (state.isLoading) {
                ComandaAiLoadingView(
                    loadingImage = painterResource(Res.drawable.golden_loading)
                )
            } else if (state.error != null) {
                ErrorView(
                    error = state.error as? ComandaAiException
                        ?: ComandaAiException.UnknownException(
                            state.error.message ?: "Unknown error"
                        )
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    ComandaAiTopAppBar(
                        title = "Detalhes do Pagamento",
                        onBackOrClose = { action(PartialPaymentDetailsAction.BACK) },
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(ComandaAiSpacing.Medium.value)
                    ) {
                        // Payment Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ComandaAiTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(ComandaAiSpacing.Medium.value)
                            ) {
                                // Amount
                                PaymentDetailRow(
                                    label = "Valor",
                                    value = state.payment?.amountFormatted ?: "",
                                    valueColor = ComandaAiTheme.colorScheme.primary,
                                    isHighlighted = true
                                )

                                Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                                HorizontalDivider(color = ComandaAiTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                                // Paid By
                                PaymentDetailRow(
                                    label = "Pago por",
                                    value = state.payment?.paidBy ?: "Cliente"
                                )

                                Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))

                                // Payment Method
                                state.payment?.paymentMethod?.let { method ->
                                    PaymentDetailRow(
                                        label = "Forma de pagamento",
                                        value = when (method) {
                                            "PIX" -> "PIX"
                                            "CARTAO" -> "Cartão"
                                            "DINHEIRO" -> "Dinheiro"
                                            else -> method
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                                }

                                // Received By
                                state.payment?.receivedBy?.let { receivedBy ->
                                    PaymentDetailRow(
                                        label = "Recebido por",
                                        value = receivedBy
                                    )
                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                                }

                                // Description
                                state.payment?.description?.let { description ->
                                    PaymentDetailRow(
                                        label = "Observação",
                                        value = description
                                    )
                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.xSmall.value))
                                }

                                // Date/Time
                                state.payment?.createdAt?.let { dateTime ->
                                    PaymentDetailRow(
                                        label = "Data/Hora",
                                        value = DateTimeFormatter.formatDateTime(dateTime)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Buttons
                        Column {
                            ComandaAiButton(
                                text = "Cancelar Pagamento",
                                onClick = { action(PartialPaymentDetailsAction.SHOW_CANCEL_CONFIRMATION) },
                                variant = ComandaAiButtonVariant.Destructive,
                                isEnabled = !state.isProcessing && state.payment?.status == "PAID",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                            ComandaAiButton(
                                text = "Voltar",
                                onClick = { action(PartialPaymentDetailsAction.BACK) },
                                variant = ComandaAiButtonVariant.Secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Cancel Confirmation Modal
            if (state.showCancelConfirmation) {
                CancelPaymentConfirmationModal(
                    amount = state.payment?.amountFormatted ?: "",
                    isVisible = state.showCancelConfirmation,
                    onDismiss = { action(PartialPaymentDetailsAction.HIDE_CANCEL_CONFIRMATION) },
                    onConfirm = { action(PartialPaymentDetailsAction.CONFIRM_CANCEL) }
                )
            }
    }
}

@Composable
private fun PaymentDetailRow(
    label: String,
    value: String,
    valueColor: Color = ComandaAiTheme.colorScheme.onSurface,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = ComandaAiTypography.bodyMedium,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = if (isHighlighted) ComandaAiTypography.titleMedium else ComandaAiTypography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
private fun CancelPaymentConfirmationModal(
    amount: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Cancelar pagamento",
        presentationMode = ComandaAiModalPresentationMode.Dynamic,
        actions = {
            Column {
                ComandaAiButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    variant = ComandaAiButtonVariant.Secondary,
                )

                ComandaAiButton(
                    text = "Confirmar",
                    onClick = onConfirm,
                    variant = ComandaAiButtonVariant.Destructive,
                )
            }
        },
        onDismiss = onDismiss,
    ) {
        Text(
            text = "Cancelar pagamento de $amount?",
            style = ComandaAiTypography.bodyMedium,
            color = ComandaAiTheme.colorScheme.onSurface,
            modifier = Modifier.padding(20.dp)
        )
    }
}
