package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = getStatusColor(currentStatus).copy(alpha = 0.1f),
                contentColor = getStatusColor(currentStatus)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "#$unitIndex",
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ItemStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusIndicator(status = status)
                            Text(getStatusText(status))
                        }
                    },
                    onClick = {
                        onStatusChange(status)
                        expanded = false
                    }
                )
            }
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