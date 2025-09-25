package co.kandalabs.comandaai.features.attendance.presentation.screens.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

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

        val isSkewer = item.category == ItemCategory.SKEWER
        var takeaway by remember(item.id) { mutableStateOf(false) }
        var vinaigrette by remember(item.id) { mutableStateOf(true) }
        var farofa by remember(item.id) { mutableStateOf(true) }
        var meatDoneness by remember(item.id) { mutableStateOf("Ao ponto") }
        var meatDonenessExpanded by remember { mutableStateOf(false) }

        val meatDonenessOptions = listOf("Mal passado", "Ao ponto", "Bem passado")

        fun buildCompleteObservation(): String {
            val parts = mutableListOf<String>()

            val manualObs = observation.trim()
            if (manualObs.isNotEmpty()) {
                parts.add(manualObs)
            }

            if (isSkewer) {
                if (takeaway) parts.add("Para viagem")
                if (!vinaigrette) parts.add("Sem vinagrete")
                if (!farofa) parts.add("Sem farofa")
                if (meatDoneness != "Ao ponto") parts.add(meatDoneness)
            }

            return parts.joinToString(", ")
        }

        ComandaAiTheme {
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
                        style = ComandaAiTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ComandaAiTheme.colorScheme.onSurface
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
                            unfocusedLabelColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
                            cursorColor = ComandaAiTheme.colorScheme.primary,
                            focusedBorderColor = ComandaAiTheme.colorScheme.primary,
                            focusedLabelColor = ComandaAiTheme.colorScheme.primary,
                            focusedContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = ComandaAiTheme.colorScheme.outline,
                            disabledPlaceholderColor = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            focusedPlaceholderColor = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            focusedTextColor = ComandaAiTheme.colorScheme.onSurface,
                            unfocusedTextColor = ComandaAiTheme.colorScheme.onSurface
                        )
                    )

                    // Espetinho-specific options
                    if (isSkewer) {
                        val switchColors = SwitchDefaults.colors(
                            checkedThumbColor = ComandaAiTheme.colorScheme.onPrimary,
                            checkedTrackColor = ComandaAiTheme.colorScheme.primary,
                            uncheckedThumbColor = ComandaAiTheme.colorScheme.outline,
                            uncheckedTrackColor = ComandaAiTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = ComandaAiTheme.colorScheme.outline
                        )
                        Text(
                            text = "Opções do Espetinho",
                            style = ComandaAiTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ComandaAiTheme.colorScheme.onSurface,
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
                                style = ComandaAiTheme.typography.bodyLarge,
                                color = ComandaAiTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = takeaway,
                                onCheckedChange = { takeaway = it },
                                colors = switchColors
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
                                style = ComandaAiTheme.typography.bodyLarge,
                                color = ComandaAiTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = vinaigrette,
                                onCheckedChange = { vinaigrette = it },
                                colors = switchColors
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
                                style = ComandaAiTheme.typography.bodyLarge,
                                color = ComandaAiTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = farofa,
                                onCheckedChange = { farofa = it },
                                colors = switchColors
                            )
                        }

                        // Meat doneness dropdown
                        Text(
                            text = "Ponto da carne",
                            style = ComandaAiTheme.typography.bodyLarge,
                            color = ComandaAiTheme.colorScheme.onSurface,
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
                                    focusedBorderColor = ComandaAiTheme.colorScheme.primary,
                                    focusedLabelColor = ComandaAiTheme.colorScheme.primary,
                                    focusedContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                                    unfocusedBorderColor = ComandaAiTheme.colorScheme.outline,
                                    unfocusedContainerColor = ComandaAiTheme.colorScheme.surfaceVariant,
                                    focusedTextColor = ComandaAiTheme.colorScheme.onSurface,
                                    unfocusedTextColor = ComandaAiTheme.colorScheme.onSurface,
                                    focusedTrailingIconColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
                                    unfocusedTrailingIconColor = ComandaAiTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = meatDonenessExpanded,
                                onDismissRequest = { meatDonenessExpanded = false },
                                containerColor = ComandaAiTheme.colorScheme.surface,
                            ) {
                                meatDonenessOptions.forEachIndexed { index, option ->
                                    DropdownMenuItem(
                                        text = {
                                            Column(
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Text(option, modifier = Modifier.padding(top = ComandaAiSpacing.Medium.value))
                                                if (index != meatDonenessOptions.lastIndex)
                                                    HorizontalDivider(
                                                        color = ComandaAiTheme.colorScheme.outlineVariant,
                                                        modifier = Modifier.padding(top = ComandaAiSpacing.Medium.value)
                                                    )
                                                else
                                                    Spacer(Modifier.padding(bottom = ComandaAiSpacing.Medium.value))
                                            }
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor = ComandaAiTheme.colorScheme.onSurface,
                                            disabledTextColor = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        ),
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
}