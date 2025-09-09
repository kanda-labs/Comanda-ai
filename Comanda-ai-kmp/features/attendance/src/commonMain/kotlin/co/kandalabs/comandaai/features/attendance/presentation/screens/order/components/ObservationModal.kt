package co.kandalabs.comandaai.features.attendance.presentation.screens.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.domain.Item

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
        val modalHeight = 400.dp
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .height(modalHeight)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = "Observação",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Item name
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Observation text field - expanded to fill available space
                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        label = { Text("Adicione informações sobre este item") },
                        placeholder = { Text("Ex: Sem cebola, ponto da carne, etc.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    // Buttons column - Secondary button first (top), Primary button last (bottom)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Back button (secondary, on top)
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Voltar")
                        }
                        
                        // Add/Save button (primary, on bottom)
                        ComandaAiButton(
                            text = if (hasItemsSelected) "Salvar" else "Adicionar",
                            onClick = { 
                                onAddWithObservation(observation.trim())
                                observation = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}