package co.kandalabs.comandaai.presentation.screens.order.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuantitySelector(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 0,
    maxValue: Int = 99
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onDecrement,
            enabled = count > minValue,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(
                text = "-",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 24.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Button(
            onClick = onIncrement,
            enabled = count < maxValue,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}