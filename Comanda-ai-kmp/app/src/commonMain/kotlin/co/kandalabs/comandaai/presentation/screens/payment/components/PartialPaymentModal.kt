package co.kandalabs.comandaai.presentation.screens.payment.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
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
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val confirmPayment = {
        if (amountText.text.isNotBlank()) {
            val amountInCentavos = CurrencyFormatter.brlToCentavos(amountText.text)
            val finalPaidBy = paidBy.trim().takeIf { it.isNotBlank() } ?: ""
            onConfirm(
                finalPaidBy,
                amountInCentavos,
                description.trim().takeIf { it.isNotBlank() }
            )
            keyboardController?.hide() // Fecha o teclado após confirmar
        }
    }

    // Auto-focus no campo de valor quando o modal abre
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Adicionar Pagamento Parcial",
        onDismiss = onDismiss,
        modifier = Modifier
            .imePadding()
            .fillMaxHeight(),
        actions = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                ComandaAiButton(
                    text = "Cancelar",
                    onClick = {
                        onDismiss()
                        keyboardController?.hide() // Fecha o teclado ao cancelar
                    },
                    isEnabled = amountText.text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    variant = ComandaAiButtonVariant.Secondary
                )

                ComandaAiButton(
                    text = "Confirmar",
                    onClick = confirmPayment,
                    isEnabled = amountText.text.isNotBlank(),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                )
            }
        }
    ) {
        // Form fields section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true) // Ocupa o espaço disponível e ajusta
                .verticalScroll(scrollState)
                .imePadding() // Padding para o conteúdo
                .padding(horizontal = ComandaAiSpacing.Large.value), // Padding padrão nas laterais
            verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value)
        ) {
            OutlinedTextField(
                value = paidBy,
                onValueChange = { paidBy = it },
                label = { Text("Quem está pagando? (opcional)") },
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
                    if (digitsOnly.length <= 8) {
                        val formatted = if (digitsOnly.isEmpty()) {
                            ""
                        } else {
                            CurrencyFormatter.formatToBRL(digitsOnly)
                        }

                        amountText = TextFieldValue(
                            text = formatted,
                            selection = TextRange(formatted.length)
                        )
                    }
                },
                label = { Text("Valor") },
                placeholder = { Text("R$ 0,00") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { confirmPayment() }
                ),
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { confirmPayment() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}