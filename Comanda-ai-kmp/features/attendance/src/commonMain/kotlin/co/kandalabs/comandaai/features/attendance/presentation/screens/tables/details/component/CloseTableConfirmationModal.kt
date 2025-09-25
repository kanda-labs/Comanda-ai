package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.component


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode


@Composable
internal fun CloseTableConfirmationModal(
    tableNumber: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Fechar Conta",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Small,
        actions = {
            ComandaAiButton(
                text = "Fechar Conta",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                variant = ComandaAiButtonVariant.Destructive
            )

            ComandaAiButton(
                text = "Cancelar",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = ComandaAiButtonVariant.Secondary
            )
        }
    ) {
        Text(
            text = "Tem certeza que deseja fechar a conta da Mesa ${tableNumber.toString().padStart(2, '0')}?",
            style = ComandaAiTheme.typography.bodyLarge,
            color = ComandaAiTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
