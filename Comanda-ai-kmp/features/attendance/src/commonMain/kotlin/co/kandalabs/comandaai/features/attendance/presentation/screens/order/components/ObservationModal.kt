package co.kandalabs.comandaai.features.attendance.presentation.screens.order.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ObservationModal(
    isVisible: Boolean,
    item: Item?,
    currentObservation: String?,
    hasItemsSelected: Boolean = false,
    onDismiss: () -> Unit,
    onAddWithObservation: (observation: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible && item != null) {
        var observation by remember(item.id) { mutableStateOf(currentObservation ?: "") }

        // Espetinho-specific options
        val isSkewer = item.category == ItemCategory.SKEWER
        var takeaway by remember(item.id) { mutableStateOf(false) }
        var vinaigrette by remember(item.id) { mutableStateOf(true) }
        var farofa by remember(item.id) { mutableStateOf(true) }
        var meatDoneness by remember(item.id) { mutableStateOf("Ao ponto") }
        var meatDonenessExpanded by remember { mutableStateOf(false) }

        val meatDonenessOptions = listOf("Mal passado", "Ao ponto", "Bem passado")

        // Function to build complete observation text
        fun buildCompleteObservation(): String {
            val parts = mutableListOf<String>()

            // Add manual observation if not empty
            val manualObs = observation.trim()
            if (manualObs.isNotEmpty()) {
                parts.add(manualObs)
            }

            // Add espetinho-specific options if item is skewer and options differ from defaults
            if (isSkewer) {
                if (takeaway) parts.add("Para viagem")
                if (!vinaigrette) parts.add("Sem vinagrete")
                if (!farofa) parts.add("Sem farofa")
                if (meatDoneness != "Ao ponto") parts.add(meatDoneness)
            }

            return parts.joinToString(", ")
        }

        ComandaAiBottomSheetModal(
            isVisible = true,
            title = "Observação",
            onDismiss = onDismiss,
            modifier = modifier,
            presentationMode = ComandaAiModalPresentationMode.Full,
            dismissOnBackgroundClick = true,
            dismissOnDrag = true,
            actions = {
                // Back button (secondary, first)
                ComandaAiButton(
                    text = "Voltar",
                    onClick = onDismiss,
                    variant = ComandaAiButtonVariant.Secondary
                )

                // Add/Save button (primary, second)
                ComandaAiButton(
                    text = if (hasItemsSelected) "Salvar" else "Adicionar",
                    onClick = {
                        onAddWithObservation(buildCompleteObservation())
                        observation = ""
                        // Reset espetinho options to defaults
                        if (isSkewer) {
                            takeaway = false
                            vinaigrette = true
                            farofa = true
                            meatDoneness = "Ao ponto"
                        }
                    }
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item name
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Observation text field
                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Observações adicionais") },
                    placeholder = { Text("Ex: Sem cebola, bem temperado, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Espetinho-specific options
                if (isSkewer) {
                    Text(
                        text = "Opções do Espetinho",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Takeaway switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Para viagem",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = takeaway,
                            onCheckedChange = { takeaway = it }
                        )
                    }

                    // Vinaigrette switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vinagrete",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = vinaigrette,
                            onCheckedChange = { vinaigrette = it }
                        )
                    }

                    // Farofa switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Farofa",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = farofa,
                            onCheckedChange = { farofa = it }
                        )
                    }

                    // Meat doneness dropdown
                    Text(
                        text = "Ponto da carne",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = meatDonenessExpanded,
                        onExpandedChange = { meatDonenessExpanded = !meatDonenessExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = meatDoneness,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = meatDonenessExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = meatDonenessExpanded,
                            onDismissRequest = { meatDonenessExpanded = false }
                        ) {
                            meatDonenessOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        meatDoneness = option
                                        meatDonenessExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}