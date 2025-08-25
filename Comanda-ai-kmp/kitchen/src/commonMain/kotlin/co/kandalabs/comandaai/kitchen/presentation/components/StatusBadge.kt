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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemStatus

@Composable
fun StatusBadge(
    status: ItemStatus,
    count: Int,
    isMultipleItems: Boolean = false,
    isDeliveredView: Boolean = false,
    onClick: () -> Unit = {}
) {
    val (backgroundColor, textColor, icon) = when (status) {
        ItemStatus.OPEN -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            Icons.Default.Schedule
        )
        ItemStatus.IN_PRODUCTION -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.PlayArrow
        )
        ItemStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            Icons.Default.Done
        )
        ItemStatus.DELIVERED -> Triple(
            androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.15f),
            androidx.compose.ui.graphics.Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
        ItemStatus.GRANTED -> Triple(
            androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.15f),
            androidx.compose.ui.graphics.Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
        ItemStatus.CANCELED -> Triple(
            androidx.compose.ui.graphics.Color(0xFF757575).copy(alpha = 0.15f),
            androidx.compose.ui.graphics.Color(0xFF757575),
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Text(
                text = getStatusText(status),
                style = MaterialTheme.typography.labelMedium,
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
        ItemStatus.OPEN -> "Pendente"
        ItemStatus.IN_PRODUCTION -> "Produzindo"
        ItemStatus.COMPLETED -> "Pronto"
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.GRANTED -> "Concluído"
        ItemStatus.CANCELED -> "Cancelado"
    }
}