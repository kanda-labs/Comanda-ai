package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus

@Composable
fun ItemUnitControl(
    unitIndex: Int,
    currentStatus: ItemStatus,
    onStatusChange: (ItemStatus) -> Unit
) {
    OutlinedButton(
        onClick = {
            // Toggle between OPEN and DELIVERED
            val newStatus = if (currentStatus == ItemStatus.DELIVERED) {
                ItemStatus.OPEN
            } else {
                ItemStatus.DELIVERED
            }
            onStatusChange(newStatus)
        },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
            contentColor = getStatusColor(currentStatus)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StatusIndicator(status = currentStatus)
            Text(
                text = "#$unitIndex",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun StatusIndicator(status: ItemStatus) {
    Card(
        modifier = Modifier.size(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(status)
        ),
        shape = androidx.compose.foundation.shape.CircleShape
    ) {}
}

@Composable
private fun getStatusColor(status: ItemStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        ItemStatus.OPEN -> MaterialTheme.colorScheme.error
        ItemStatus.IN_PRODUCTION -> MaterialTheme.colorScheme.primary
        ItemStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
        ItemStatus.DELIVERED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        ItemStatus.CANCELED -> androidx.compose.ui.graphics.Color(0xFF757575)
    }
}

private fun getStatusText(status: ItemStatus): String {
    return when (status) {
        ItemStatus.OPEN -> "Pendente"
        ItemStatus.IN_PRODUCTION -> "Produzindo"
        ItemStatus.COMPLETED -> "Pronto"
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.CANCELED -> "Cancelado"
    }
}