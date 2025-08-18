package co.kandalabs.comandaai.presentation.screens.ordersline.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import kandalabs.commander.domain.model.ItemStatus
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.OrderStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

@Composable
fun OrderCard(
    order: Order,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onMarkComplete: () -> Unit,
    onMarkItemComplete: (Int) -> Unit
) {
    var isExpanded by remember { mutableStateOf(isSelected) }
    
    LaunchedEffect(isSelected) {
        isExpanded = isSelected
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { 
                isExpanded = !isExpanded
                onCardClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = when (order.status) {
                OrderStatus.OPEN -> MaterialTheme.colorScheme.surface
                OrderStatus.GRANTED -> ComandaAiColors.Green100.value
                OrderStatus.CANCELED -> ComandaAiColors.Error.value.copy(alpha = 0.1f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                    ) {
                        Text(
                            text = "Pedido #${order.id}",
                            style = ComandaAiTypography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OrderStatusBadge(status = order.status)
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value)
                    ) {
                        Text(
                            text = "Mesa ${order.tableNumber}",
                            style = ComandaAiTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = getElapsedTime(order.createdAt),
                            style = ComandaAiTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (order.status == OrderStatus.OPEN) {
                        IconButton(onClick = onMarkComplete) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Marcar como completo",
                                tint = ComandaAiColors.Green600.value
                            )
                        }
                    }
                    
                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir"
                    )
                }
            }
            
            // Items preview when collapsed
            if (!isExpanded) {
                Text(
                    text = "${order.items.size} item(s) • ${order.items.filter { it.status == ItemStatus.OPEN }.size} pendente(s)",
                    style = ComandaAiTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = ComandaAiSpacing.xXSmall.value)
                )
            }
            
            // Expanded content
            if (isExpanded) {
HorizontalDivider(
                    modifier = Modifier.padding(vertical = ComandaAiSpacing.Small.value)
                )
                
                order.items.forEach { item ->
                    OrderItemRow(
                        item = item,
                        onMarkComplete = { 
                            if (order.status == OrderStatus.OPEN) {
                                onMarkItemComplete(item.itemId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        OrderStatus.OPEN -> Triple(
            ComandaAiColors.Yellow600.value,
            ComandaAiColors.Gray900.value,
            "Pendente"
        )
        OrderStatus.GRANTED -> Triple(
            ComandaAiColors.Green600.value,
            ComandaAiColors.Surface.value,
            "Atendido"
        )
        OrderStatus.CANCELED -> Triple(
            ComandaAiColors.Error.value,
            ComandaAiColors.OnError.value,
            "Cancelado"
        )
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = ComandaAiSpacing.Small.value, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = ComandaAiTypography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OrderItemRow(
    item: kandalabs.commander.domain.model.ItemOrder,
    onMarkComplete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ComandaAiSpacing.xXSmall.value),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.count}x",
                    style = ComandaAiTypography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.name,
                    style = ComandaAiTypography.bodyMedium,
                    color = if (item.status == ItemStatus.GRANTED) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            item.observation?.let { obs ->
                Text(
                    text = "Obs: $obs",
                    style = ComandaAiTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = ComandaAiSpacing.Large.value)
                )
            }
        }
        
        when (item.status) {
            ItemStatus.GRANTED -> {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completo",
                    tint = ComandaAiColors.Green600.value,
                    modifier = Modifier.size(20.dp)
                )
            }
            ItemStatus.DELIVERED -> {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Entregue",
                    tint = ComandaAiColors.Green600.value,
                    modifier = Modifier.size(20.dp)
                )
            }
            ItemStatus.OPEN -> {
                IconButton(
                    onClick = onMarkComplete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Marcar como completo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            ItemStatus.CANCELED -> {
                Text(
                    text = "Cancelado",
                    style = ComandaAiTypography.labelSmall,
                    color = ComandaAiColors.Error.value
                )
            }
        }
    }
}

private fun getElapsedTime(createdAt: kotlinx.datetime.LocalDateTime): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    
    // Calculate difference in minutes (simple approach)
    val nowMillis = now.year * 525600 + now.monthNumber * 43800 + now.dayOfMonth * 1440 + now.hour * 60 + now.minute
    val createdMillis = createdAt.year * 525600 + createdAt.monthNumber * 43800 + createdAt.dayOfMonth * 1440 + createdAt.hour * 60 + createdAt.minute
    val minutesElapsed = (nowMillis - createdMillis).coerceAtLeast(0)
    
    return when {
        minutesElapsed < 1 -> "Agora"
        minutesElapsed < 60 -> "Há $minutesElapsed min"
        else -> "Há ${minutesElapsed / 60}h ${minutesElapsed % 60}min"
    }
}
