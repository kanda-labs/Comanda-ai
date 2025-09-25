package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTextButton
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

@Composable
internal fun UserProfileModal(
    isVisible: Boolean,
    userName: String?,
    userRole: String?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComandaAiBottomSheetModal(
        isVisible = isVisible,
        title = "Perfil do Usuário",
        onDismiss = onDismiss,
        actions = {
            ComandaAiButton(
                text = "Logout",
                onClick = {
                    onLogout()
                    onDismiss()
                },
                variant = ComandaAiButtonVariant.Destructive
            )

            ComandaAiTextButton(
                text = "Cancelar",
                onClick = onDismiss
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ComandaAiSpacing.Large.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            // User Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(ComandaAiTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (userName?.isNotEmpty() == true) {
                    Text(
                        text = userName.first().uppercaseChar().toString(),
                        style = ComandaAiTheme.typography.headlineLarge,
                        color = ComandaAiTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar do usuário",
                        modifier = Modifier.size(40.dp),
                        tint = ComandaAiTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

            // User Name
            Text(
                text = userName ?: "Usuário",
                style = ComandaAiTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ComandaAiTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

            // User Role
            Text(
                text = userRole?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Função",
                style = ComandaAiTheme.typography.bodyLarge,
                color = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))
        }
    }
}