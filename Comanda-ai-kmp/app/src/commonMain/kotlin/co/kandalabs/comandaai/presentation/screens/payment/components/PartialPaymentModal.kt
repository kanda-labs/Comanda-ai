package co.kandalabs.comandaai.presentation.screens.payment.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.utils.CurrencyFormatter

@Composable
internal fun PartialPaymentModal(
    onDismiss: () -> Unit,
    onConfirm: (paidBy: String, amountInCentavos: Long, description: String?) -> Unit
) {
    var paidBy by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf("") }

    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Adicionar Pagamento Parcial",
        onDismiss = onDismiss,
        actions = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }

            ComandaAiButton(
                text = "Confirmar",
                onClick = {
                    if (paidBy.isNotBlank() && amountText.text.isNotBlank()) {
                        val amountInCentavos =
                            CurrencyFormatter.brlToCentavos(amountText.text)
                        onConfirm(
                            paidBy.trim(),
                            amountInCentavos,
                            description.trim().takeIf { it.isNotBlank() }
                        )
                    }
                },
                isEnabled = paidBy.isNotBlank() && amountText.text.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        // Form fields section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value)
        ) {
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
        }
    }
}
