package co.kandalabs.comandaai.features.attendance.presentation.screens.order.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.features.attendance.domain.model.ItemWithCount

@Composable
fun OrderConfirmationModal(
    isVisible: Boolean,
    selectedItems: List<ItemWithCount>,
    totalItems: Int,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                )
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f) // Max 80% of screen height
                        .clickable(enabled = false) { }, // Prevent clicks from propagating to background
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = ComandaAiTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                ) {
                    // Handle/Drag indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    color = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Header
                    Text(
                        text = "Confirmar Pedido",
                        style = ComandaAiTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ComandaAiTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    
                    // Items list - takes most of the space
                    if (selectedItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum item selecionado",
                                style = ComandaAiTheme.typography.bodyMedium,
                                color = ComandaAiTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Itens selecionados:",
                                style = ComandaAiTheme.typography.titleMedium,
                                color = ComandaAiTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                            
                            // Scrollable items list - takes remaining space
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedItems) { itemWithCount ->
                                    OrderConfirmationItem(
                                        itemWithCount = itemWithCount
                                    )
                                }
                            }
                        }
                    }
                    
                    // Fixed total and action buttons at bottom
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 4.dp,
                        color = ComandaAiTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Total - always visible above buttons
                            if (selectedItems.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ComandaAiTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Total:",
                                            style = ComandaAiTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ComandaAiTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "$totalItems ${if (totalItems == 1) "item" else "itens"}",
                                            style = ComandaAiTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ComandaAiTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            // Action buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Cancel button
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text("Cancelar")
                                }
                                
                                // Confirm button
                                ComandaAiButton(
                                    text = if (isLoading) "Enviando..." else "Confirmar",
                                    onClick = onConfirm,
                                    isEnabled = !isLoading && selectedItems.isNotEmpty(),
                                    modifier = Modifier.weight(1f)
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

@Composable
private fun OrderConfirmationItem(
    itemWithCount: ItemWithCount,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = itemWithCount.item.name,
                    style = ComandaAiTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.onSurfaceVariant
                )
                itemWithCount.item.description?.let { description ->
                    Text(
                        text = description,
                        style = ComandaAiTheme.typography.bodySmall,
                        color = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Text(
                text = "${itemWithCount.count}x",
                style = ComandaAiTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ComandaAiTheme.colorScheme.primary
            )
        }
    }
}