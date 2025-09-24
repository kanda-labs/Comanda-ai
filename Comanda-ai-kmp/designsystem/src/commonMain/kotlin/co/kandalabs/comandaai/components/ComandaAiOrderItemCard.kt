package co.kandalabs.comandaai.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.core.utils.CurrencyFormatter

data class OrderItemData(
    val name: String,
    val value: Int,
    val description: String? = null,
    val count: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComandaAiOrderItemCard(
    item: OrderItemData,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colorScheme.gray200
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
                    text = item.name,
                    style = ComandaAiTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = CurrencyFormatter.formatCents(item.value),
                    style = ComandaAiTheme.typography.bodyMedium,
                    color = ComandaAiTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                item.description?.let { description ->
                    if (description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = ComandaAiTheme.typography.bodySmall,
                            color = ComandaAiTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            ComandaAiQuantitySelector(
                count = item.count,
                onIncrement = onIncrement,
                onDecrement = onDecrement
            )
        }
    }
}

