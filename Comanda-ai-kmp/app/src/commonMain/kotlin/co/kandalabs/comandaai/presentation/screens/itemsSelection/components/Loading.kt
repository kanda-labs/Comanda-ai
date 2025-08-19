package co.kandalabs.comandaai.presentation.screens.itemsSelection.components

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
import androidx.compose.ui.platform.testTag
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.app.generated.resources.Res
import comandaai.app.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoadingView(testTag: String? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(Res.drawable.golden_loading),
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