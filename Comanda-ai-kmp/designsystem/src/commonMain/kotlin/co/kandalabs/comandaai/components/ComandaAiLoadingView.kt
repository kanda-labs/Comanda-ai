package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

@Composable
fun ComandaAiLoadingView(
    loadingImage: Painter? = null, // Mantém compatibilidade, mas ignora a imagem
    testTag: String? = null,
    modifier: Modifier = Modifier,
    message: String = "Carregando..."
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            CircularProgressIndicator(
                color = ComandaAiTheme.colorScheme.primary,
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Large.value))

            // Loading message
            Text(
                text = message,
                style = ComandaAiTheme.typography.bodyLarge,
                color = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}