package co.touchlab.dogify.presentation.screens.order.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.dogify.domain.model.ItemWithCount

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OrderItemCard(
    itemWithCount: ItemWithCount,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onIncrement,
                    onLongClick = onLongClick
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = itemWithCount.item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatPrice(itemWithCount.item.value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                itemWithCount.item.description?.let { description ->
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            QuantitySelector(
                count = itemWithCount.count,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )
        }
    }
}

private fun formatPrice(valueInCents: Int): String {
    val valueInReais = valueInCents / 100.0
    val formatted = (valueInReais * 100).toInt().let { cents ->
        val reais = cents / 100
        val centavos = cents % 100
        "$reais,${centavos.toString().padStart(2, '0')}"
    }
    return "R$ $formatted"
}