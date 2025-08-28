package co.kandalabs.comandaai.presentation.screens.payment.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.presentation.screens.payment.PaymentSummaryScreenState


@Composable
internal fun FinishPaymentConfirmationModal(
    isVisible: Boolean,
    state: PaymentSummaryScreenState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Confirmar Finalização",
        onDismiss = onDismiss,
        actions = {

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isProcessingPayment
            ) {
                Text("Cancelar")
            }

            ComandaAiButton(
                text = if (state.isProcessingPayment) {
                    "Processando..."
                } else if (state.isFullyPaid) {
                    "Finalizar mesa"
                } else {
                    "Confirmar pagamento"
                },
                onClick = onConfirm,
                isEnabled = !state.isProcessingPayment,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        // Confirmation message
        Text(
            text = if (state.isFullyPaid) {
                "Tem certeza que deseja finalizar esta mesa?\n\nA conta está totalmente paga."
            } else if (state.hasPartialPayments) {
                "Tem certeza que deseja finalizar o pagamento?\n\nRestante: ${state.remainingAmountPresentation}"
            } else {
                "Tem certeza que deseja finalizar o pagamento?\n\nTotal: ${state.totalAmountPresentation}"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}