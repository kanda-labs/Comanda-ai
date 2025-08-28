package co.kandalabs.comandaai.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

@Composable
fun ComandaAiLoadingView(
    loadingImage: Painter,
    testTag: String? = null,
    modifier: Modifier = Modifier
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
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = loadingImage,
                contentDescription = "Loading Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComandaAiSpacing.xXLarge.value)
                    .aspectRatio(1f)
            )
            LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.weight(3f))
        }
    }
}