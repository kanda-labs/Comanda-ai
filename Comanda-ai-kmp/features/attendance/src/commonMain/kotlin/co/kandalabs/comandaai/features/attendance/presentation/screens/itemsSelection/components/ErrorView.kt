package co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.sdk.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ErrorView(
    error: ComandaAiException,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(ComandaAiSpacing.Medium.value)
    ) {
        // Content area (scrollable if needed)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Select appropriate error icon and color
            val (errorIcon, iconColor) = when (error) {
                is ComandaAiException.NoInternetConnectionException ->
                    Icons.Default.CloudOff to ComandaAiTheme.colorScheme.error
                is ComandaAiException.UnknownHttpException ->
                    Icons.Default.Warning to ComandaAiTheme.colorScheme.error
                else ->
                    Icons.Default.ErrorOutline to ComandaAiTheme.colorScheme.error
            }

            // Display error icon
            Icon(
                imageVector = errorIcon,
                contentDescription = "Error",
                modifier = Modifier.size(80.dp),
                tint = iconColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            // Display error title
            val errorTitle = when (error) {
                is ComandaAiException.NoInternetConnectionException -> "Sem conexão"
                is ComandaAiException.UnknownHttpException -> "Erro de rede"
                else -> "Algo deu errado"
            }

            Text(
                text = errorTitle,
                style = ComandaAiTheme.typography.headlineSmall,
                color = ComandaAiTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

            // Display error-specific message
            val errorMessage = when (error) {
                is ComandaAiException.NoInternetConnectionException ->
                    "Verifique sua conexão com a internet e tente novamente."
                is ComandaAiException.UnknownHttpException ->
                    "Problema na comunicação com o servidor. Tente novamente."
                is ComandaAiException.UnknownException ->
                    "Ocorreu um erro inesperado. Tente novamente."
                else ->
                    "Ocorreu um problema. Por favor, tente novamente."
            }

            Text(
                text = errorMessage,
                style = ComandaAiTheme.typography.bodyLarge,
                color = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // Bottom button area
        onRetry?.let {
            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
            ComandaAiButton(
                text = "Tentar novamente",
                onClick = it,
                variant = ComandaAiButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}