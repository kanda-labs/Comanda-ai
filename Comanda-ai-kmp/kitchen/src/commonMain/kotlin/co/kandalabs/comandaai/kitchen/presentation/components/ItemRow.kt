package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail

@Composable
fun ItemRow(
    item: KitchenItemDetail,
    onStatusChange: (Int, ItemStatus) -> Unit,
    onMarkItemAsDelivered: (Int) -> Unit,
    isDeliveredView: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasUndeliveredItems = item.unitStatuses.any { it.status != ItemStatus.DELIVERED }
    
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Mostrar quantidade
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${item.totalCount}x",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                if (!item.observation.isNullOrBlank()) {
                    Text(
                        text = "Obs: ${item.observation}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            StatusBadge(
                status = item.overallStatus,
                count = item.unitStatuses.count { it.status != ItemStatus.DELIVERED },
                isMultipleItems = item.totalCount > 1,
                isDeliveredView = isDeliveredView,
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
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
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                // Normal "Mark as Delivered" button
                                FilledTonalButton(
                                    onClick = { onMarkItemAsDelivered(item.itemId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
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
                                            style = MaterialTheme.typography.labelLarge,
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
