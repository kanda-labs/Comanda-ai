package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.features.attendance.domain.models.model.PartialPayment
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import kotlinx.datetime.*

@Composable
internal fun PaymentHistoryCard(
    payment: PartialPayment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with amount and payment method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payment.amountFormatted,
                    style = ComandaAiTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiTheme.colorScheme.onSurface
                )

                payment.paymentMethod?.let { method ->
                    Text(
                        text = method.displayName,
                        style = ComandaAiTheme.typography.bodyMedium,
                        color = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Date and time
            Text(
                text = formatDateTime(payment.createdAt),
                style = ComandaAiTheme.typography.bodyMedium,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant
            )

            // Table info
            Text(
                text = "Mesa ${payment.tableId}",
                style = ComandaAiTheme.typography.bodyMedium,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant
            )

            // Paid by
            Text(
                text = "Pago por: ${payment.paidBy}",
                style = ComandaAiTheme.typography.bodyMedium,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant
            )

            // Received by
            payment.receivedBy?.let { receivedBy ->
                Text(
                    text = "Recebido por: $receivedBy",
                    style = ComandaAiTheme.typography.bodySmall,
                    color = ComandaAiTheme.colorScheme.onSurfaceVariant
                )
            }

            // Description
            payment.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = "Observação: $description",
                        style = ComandaAiTheme.typography.bodySmall,
                        color = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDateTime(localDateTime: kotlinx.datetime.LocalDateTime): String {
    val date = localDateTime.date
    val time = localDateTime.time

    val formattedDate = "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
    val formattedTime = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

    return "$formattedDate às $formattedTime"
}