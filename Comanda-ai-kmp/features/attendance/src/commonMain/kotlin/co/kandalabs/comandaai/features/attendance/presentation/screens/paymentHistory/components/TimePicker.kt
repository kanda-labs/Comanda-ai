package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton

@Composable
internal fun TimePicker(
    label: String,
    hour: Int,
    minute: Int,
    onShowTimePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onShowTimePicker() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Selecionar hora",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun TimePickerModal(
    isVisible: Boolean,
    title: String,
    currentHour: Int,
    currentMinute: Int,
    onTimeChanged: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        ComandaAiBottomSheetModal(
            isVisible = true,
            title = title,
            onDismiss = onDismiss,
            actions = {
                ComandaAiButton(
                    text = "Confirmar",
                    onClick = onDismiss
                )
            }
        ) {
            TimePickerContent(
                currentHour = currentHour,
                currentMinute = currentMinute,
                onTimeChanged = onTimeChanged
            )
        }
    }
}

@Composable
private fun TimePickerContent(
    currentHour: Int,
    currentMinute: Int,
    onTimeChanged: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(currentHour) }
    var selectedMinute by remember { mutableStateOf(currentMinute) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hour selector
        TimeSelector(
            label = "Hora",
            value = selectedHour,
            maxValue = 23,
            onValueChange = { newHour ->
                selectedHour = newHour
                onTimeChanged(selectedHour, selectedMinute)
            }
        )

        // Minute selector
        TimeSelector(
            label = "Minuto",
            value = selectedMinute,
            maxValue = 59,
            onValueChange = { newMinute ->
                selectedMinute = newMinute
                onTimeChanged(selectedHour, selectedMinute)
            }
        )
    }
}

@Composable
private fun TimeSelector(
    label: String,
    value: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Minus button
            AnimatedButton(
                onClick = {
                    val newValue = if (value > 0) value - 1 else maxValue
                    onValueChange(newValue)
                },
                icon = Icons.Default.Remove,
                contentDescription = "Diminuir $label"
            )

            // Value display
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Plus button
            AnimatedButton(
                onClick = {
                    val newValue = if (value < maxValue) value + 1 else 0
                    onValueChange(newValue)
                },
                icon = Icons.Default.Add,
                contentDescription = "Aumentar $label"
            )
        }
    }
}

@Composable
private fun AnimatedButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .background(
                MaterialTheme.colorScheme.primary,
                CircleShape
            )
            .clickable {
                isPressed = true
                onClick()
            }
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )
    }

    // Reset pressed state after animation
    if (isPressed) {
        remember {
            isPressed = false
        }
    }
}