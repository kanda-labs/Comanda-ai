package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod

@Composable
internal fun PaymentMethodDropdown(
    selectedPaymentMethod: PaymentMethod?,
    onPaymentMethodSelected: (PaymentMethod?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf(
        null to "Todos os métodos",
        PaymentMethod.PIX to PaymentMethod.PIX.displayName,
        PaymentMethod.CARTAO to PaymentMethod.CARTAO.displayName,
        PaymentMethod.DINHEIRO to PaymentMethod.DINHEIRO.displayName
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedPaymentMethod?.displayName ?: "Todos os métodos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expandir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            paymentMethods.forEach { (method, displayName) ->
                DropdownMenuItem(
                    text = { Text(displayName) },
                    onClick = {
                        onPaymentMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}