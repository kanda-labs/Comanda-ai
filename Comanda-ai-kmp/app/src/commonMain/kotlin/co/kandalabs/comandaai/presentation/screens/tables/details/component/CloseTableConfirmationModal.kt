package co.kandalabs.comandaai.presentation.screens.tables.details.component


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
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
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    ) {
        Text(
            text = "Tem certeza que deseja fechar a conta da Mesa ${tableNumber.toString().padStart(2, '0')}?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
