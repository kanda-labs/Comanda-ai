package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.tokens.ComandaAiColors

@Composable
fun StatusBadge(
    status: ItemStatus,
    count: Int,
    isMultipleItems: Boolean = false,
    isDeliveredView: Boolean = false,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, textColor, icon) = when (status) {
        ItemStatus.PENDING -> Triple(
            ComandaAiColors.Error.value.copy(alpha = 0.30f),
            ComandaAiColors.OnError.value,
            Icons.Default.Schedule
        )

        ItemStatus.DELIVERED -> Triple(
            androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.15f),
            androidx.compose.ui.graphics.Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )

        ItemStatus.CANCELED -> Triple(
            ComandaAiTheme.colorScheme.surfaceVariant,
            ComandaAiTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Cancel
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = textColor
                )
            }

            if (count > 0)
                Text(
                    text = count.toString(),
                    style = ComandaAiTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

            Text(
                text = getStatusText(status),
                style = ComandaAiTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            // Show dropdown arrow for multiple items
            if (isMultipleItems) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expandir",
                    modifier = Modifier.size(16.dp),
                    tint = textColor
                )
            }
        }
    }
}

private fun getStatusText(status: ItemStatus): String {
    return when (status) {
        ItemStatus.PENDING -> "Pendente"
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.CANCELED -> "Cancelado"
    }
}