package co.kandalabs.comandaai.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

private val ButtonSizeOffset = 40.dp
private const val ActionButtonDescription = "Top bar action button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComandaAiTopAppBar(
    title: String,
    onBackOrClose: () -> Unit = {},
    icon: ImageVector? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(end = if (icon != null) ButtonSizeOffset else 0.dp),
                text = title,
                style = ComandaAiTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            icon?.let { safeIcon ->
                IconButton(onClick = onBackOrClose) {
                    Icon(
                        contentDescription = ActionButtonDescription,
                        imageVector = safeIcon,
                        tint = ComandaAiTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ComandaAiTheme.colorScheme.background,
            titleContentColor = ComandaAiTheme.colorScheme.onBackground
        )
    )
}

@Preview
@Composable
private fun CustomTopAppBarPreview() {
    ComandaAiTopAppBar(
        "My title here",
        onBackOrClose = { },
        icon = Icons.Default.Close
    )
}