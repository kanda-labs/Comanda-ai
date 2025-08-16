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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.kandalabs.comandaai.core.error.ComandaAiException
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comanda_ai_kmp.app.generated.resources.Res
import comanda_ai_kmp.app.generated.resources.golden_connection_error
import comanda_ai_kmp.app.generated.resources.golden_generic_error
import org.jetbrains.compose.resources.painterResource

@Composable
fun ErrorView(error: ComandaAiException, retry: (() -> Unit)? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        val imageError = when (error) {
            ComandaAiException.NoInternetConnectionException,
            is ComandaAiException.UnknownHttpException -> Res.drawable.golden_connection_error

            is ComandaAiException.UnknownException -> Res.drawable.golden_generic_error
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(imageError),
                    contentDescription = "Error Image",
                    modifier = Modifier
                        .padding(horizontal = ComandaAiSpacing.xXLarge.value)
                        .aspectRatio(1f)
                )
                Spacer(modifier = Modifier.padding(ComandaAiSpacing.Medium.value))
                Text(
                    text = "Ops! something went wrong",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    "code: ${error.code}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (error is ComandaAiException.NoInternetConnectionException) {
                retry?.let {
                    ComandaAiButton(
                        text = "Retry",
                        onClick = { retry() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
