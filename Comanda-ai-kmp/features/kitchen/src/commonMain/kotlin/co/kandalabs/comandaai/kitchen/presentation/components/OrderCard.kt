package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.*
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.theme.ComandaAiTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun OrderCard(
    order: KitchenOrder,
    onItemStatusChange: (Int, Int, ItemStatus) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit,
    onShowDeliveryConfirmation: (KitchenOrder) -> Unit = {},
    isDeliveredView: Boolean = false,
    loadingItemIds: Set<String> = emptySet(),
    isRemoving: Boolean = false,
    onRemovalAnimationComplete: () -> Unit = {}
) {
    val allDelivered = order.items.all { item ->
        item.unitStatuses.all { it.status == ItemStatus.DELIVERED || it.status == ItemStatus.CANCELED }
    }
    
    AnimatedVisibility(
        visible = !isRemoving,
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 300),
            targetOffsetY = { -it }
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = Modifier.animateContentSize(
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 1.dp,
            color = ComandaAiTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Enhanced Header with better visual hierarchy
            OrderHeader(order = order)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Items section with improved spacing
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                order.items.forEachIndexed { index, item ->
                    ItemRow(
                        item = item,
                        onStatusChange = { unitIndex, status ->
                            onItemStatusChange(item.itemId, unitIndex, status)
                        },
                        onMarkItemAsDelivered = { itemId ->
                            onMarkItemAsDelivered(order.id, itemId)
                        },
                        isDeliveredView = isDeliveredView,
                        loadingItemIds = loadingItemIds,
                        orderId = order.id
                    )
                    
                    if (index < order.items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = ComandaAiTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
            
            if (!isDeliveredView && !allDelivered) {
                Spacer(modifier = Modifier.height(24.dp))
                
                ComandaAiButton(
                    text = "Entregar pedido",
                    onClick = { onShowDeliveryConfirmation(order) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ComandaAiButtonVariant.Primary
                )
            }
        }
    }
    }
    
    LaunchedEffect(isRemoving) {
        if (isRemoving) {
            kotlinx.coroutines.delay(300)
            onRemovalAnimationComplete()
        }
    }
}

@Composable
fun OrderHeader(order: KitchenOrder) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Left side - Table info with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ComandaAiTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.TableRestaurant,
                        contentDescription = null,
                        tint = ComandaAiTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Mesa ${order.tableNumber}",
                    style = ComandaAiTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiTheme.colorScheme.onSurface
                )
                Text(
                    text = order.userName,
                    style = ComandaAiTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.primary
                )
                Text(
                    text = "${order.items.size} ${if (order.items.size == 1) "item" else "itens"}",
                    style = ComandaAiTheme.typography.bodyMedium,
                    color = ComandaAiTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Right side - Order details with better layout
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ComandaAiTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "Pedido #${order.id}",
                    style = ComandaAiTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            val createdTime = Instant.fromEpochMilliseconds(order.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            
            // Show creation time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = ComandaAiTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${createdTime.hour.toString().padStart(2, '0')}:${createdTime.minute.toString().padStart(2, '0')}",
                    style = ComandaAiTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.onSurface
                )
            }
            
            // Show update time if available and different from creation time
            order.updatedAt?.let { updatedAtTimestamp ->
                if (updatedAtTimestamp != order.createdAt) {
                    val updatedTime = Instant.fromEpochMilliseconds(updatedAtTimestamp)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Atualizado em: ${updatedTime.hour.toString().padStart(2, '0')}:${updatedTime.minute.toString().padStart(2, '0')}",
                        style = ComandaAiTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}