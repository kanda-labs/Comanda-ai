package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ItemRow(
    item: KitchenItemDetail,
    onStatusChange: (Int, ItemStatus) -> Unit,
    onMarkItemAsDelivered: (Int) -> Unit,
    isDeliveredView: Boolean = false,
    loadingItemIds: Set<String> = emptySet(),
    orderId: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasUndeliveredItems = item.unitStatuses.any { it.status != ItemStatus.DELIVERED }
    val itemKey = "$orderId-${item.itemId}"
    val isItemLoading = loadingItemIds.contains(itemKey)
    
    Column {
        // Header do item
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Column (
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = item.name,
                        style = ComandaAiTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = ComandaAiTheme.colorScheme.onSurface
                    )
                    
                    // Mostrar quantidade
                    Surface(
                        color = ComandaAiTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${item.totalCount}x",
                            style = ComandaAiTheme.typography.labelMedium,
                            color = ComandaAiTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                if (!item.observation.isNullOrBlank()) {
                    Text(
                        text = "Obs: ${item.observation}",
                        style = ComandaAiTheme.typography.bodyMedium,
                        color = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            StatusBadge(
                status = item.overallStatus,
                count = item.unitStatuses.count { it.status == ItemStatus.PENDING },
                isMultipleItems = item.totalCount > 1,
                isDeliveredView = isDeliveredView,
                isLoading = isItemLoading,
                onClick = {
                    if (item.totalCount > 1) {
                        // Toggle accordion for multiple items
                        isExpanded = !isExpanded
                    } else {
                        // Toggle status between OPEN and DELIVERED for single item
                        val newStatus = if (item.overallStatus == ItemStatus.DELIVERED) {
                            ItemStatus.PENDING
                        } else {
                            ItemStatus.DELIVERED
                        }
                        onStatusChange(0, newStatus)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        
        // Enhanced conditional logic with better UX
        if (item.totalCount > 1) {
            // Multiple items - expandable accordion triggered by badge click only
            
            // Enhanced accordion with smooth animations
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300),
                    expandFrom = Alignment.Top
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300),
                    shrinkTowards = Alignment.Top
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = ComandaAiTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        // Improved grid layout
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.height(((item.unitStatuses.size / 3 + 1) * 70).dp)
                        ) {
                            itemsIndexed(
                                items = item.unitStatuses,
                                key = { index, _ -> "${item.itemId}_$index" } // Chave estável única
                            ) { index, unitStatus ->
                                ItemUnitControl(
                                    unitIndex = index + 1,
                                    currentStatus = unitStatus.status,
                                    onStatusChange = { newStatus ->
                                        onStatusChange(index, newStatus)
                                    },
                                    isDeliveredView = isDeliveredView
                                )
                            }
                        }
                        
                        // Quick action button for all items
                        if ((!isDeliveredView && hasUndeliveredItems) || (isDeliveredView && !hasUndeliveredItems)) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isDeliveredView && !hasUndeliveredItems) {
                                // In delivered view, show "Revert to Pending" button when all items are delivered
                                FilledTonalButton(
                                    onClick = { 
                                        // Revert first item status to OPEN to move order back to active
                                        onStatusChange(0, ItemStatus.PENDING)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = ComandaAiTheme.colorScheme.secondary,
                                        contentColor = ComandaAiTheme.colorScheme.onSecondary
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Reverter para Pendente",
                                            style = ComandaAiTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                // Normal "Mark as Delivered" button
                                FilledTonalButton(
                                    onClick = { onMarkItemAsDelivered(item.itemId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isItemLoading,
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = ComandaAiTheme.colorScheme.primary,
                                        contentColor = ComandaAiTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Marcar Todos como Entregues",
                                            style = ComandaAiTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium
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
}
