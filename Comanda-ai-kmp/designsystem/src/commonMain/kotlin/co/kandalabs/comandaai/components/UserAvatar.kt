package co.kandalabs.comandaai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun UserAvatar(
    userName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(ComandaAiTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!userName.isNullOrEmpty()) {
            Text(
                text = userName.first().uppercaseChar().toString(),
                style = ComandaAiTheme.typography.titleMedium,
                color = ComandaAiTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar do usuário",
                modifier = Modifier.size(20.dp),
                tint = ComandaAiTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarPreview() {
    MaterialTheme {
        UserAvatar(
            userName = "João Silva",
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun UserAvatarEmptyPreview() {
    MaterialTheme {
        UserAvatar(
            userName = null,
            onClick = {}
        )
    }
}