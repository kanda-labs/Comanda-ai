package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ComandaAiQuantitySelector(
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

        val buttonColors =
                ButtonDefaults.buttonColors(
                    containerColor = ComandaAiTheme.colorScheme.primary,
                    contentColor = ComandaAiTheme.colorScheme.onPrimary,
                    disabledContentColor = ComandaAiTheme.colorScheme.gray700,
                    disabledContainerColor = ComandaAiTheme.colorScheme.gray300
                )

        Button(
            onClick = onDecrement,
            enabled = count > minValue,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(4.dp),
            colors = buttonColors
        ) {
            Text(
                text = "-",
                style = ComandaAiTheme.typography.titleMedium
            )
        }

        Text(
            text = count.toString(),
            style = ComandaAiTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = 24.dp),
            textAlign = TextAlign.Center,
            color = ComandaAiTheme.colorScheme.onSurface
        )

        Button(
            onClick = onIncrement,
            enabled = count < maxValue,
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(4.dp),
            colors = buttonColors
        ) {
            Text(
                text = "+",
                style = ComandaAiTheme.typography.titleMedium
            )
        }
    }
}