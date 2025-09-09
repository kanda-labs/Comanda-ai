package co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.sdk.error.ComandaAiException
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.theme.ComandaAiTypography
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_connection_error
import comandaai.features.attendance.generated.resources.golden_generic_error
import org.jetbrains.compose.resources.painterResource

@Composable
fun ErrorView(
    error: ComandaAiException,
    onRetry: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ComandaAiSpacing.Medium.value),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Select appropriate error image
            val imageError = when (error) {
                is ComandaAiException.NoInternetConnectionException,
                is ComandaAiException.UnknownHttpException -> Res.drawable.golden_connection_error
                else -> Res.drawable.golden_generic_error
            }

            // Display error image
            Image(
                painter = painterResource(imageError),
                contentDescription = "Error Image",
                modifier = Modifier
                    .padding(horizontal = ComandaAiSpacing.xXLarge.value)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            // Display error-specific message
            val errorMessage = when (error) {
                is ComandaAiException.NoInternetConnectionException ->
                    "No internet connection. Please check your network and try again."
                is ComandaAiException.UnknownHttpException ->
                    "Network error (Code: ${error.code}). Please try again."
                is ComandaAiException.UnknownException ->
                    "An unexpected error occurred. Please try again."
                else ->
                    "Something went wrong. Please try again."
            }

            Text(
                text = errorMessage,
                style = ComandaAiTypography.titleLarge,
                color = ComandaAiColors.Error.value,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

            // Display error code for debugging
            Text(
                text = "Code: ${error.code}",
                style = ComandaAiTypography.bodySmall,
                color = ComandaAiColors.Gray600.value
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Large.value))

            // Display retry button if onRetry is provided
            onRetry?.let {
                ComandaAiButton(
                    text = "Retry",
                    onClick = it,
                    variant = ComandaAiButtonVariant.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = ComandaAiSpacing.Medium.value)
                )
            }
        }
    }
}