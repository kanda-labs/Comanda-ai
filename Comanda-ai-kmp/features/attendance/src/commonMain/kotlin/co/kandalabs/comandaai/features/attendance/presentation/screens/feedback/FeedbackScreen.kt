package co.kandalabs.comandaai.features.attendance.presentation.screens.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiButton
import kotlinx.coroutines.delay

class FeedbackScreen(
    private val isSuccess: Boolean,
    private val message: String,
    private val autoCloseDelayMs: Long = 3000
) : Screen {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        LaunchedEffect(Unit) {
            if (isSuccess) {
                delay(autoCloseDelayMs)
                navigator.pop()
            }
        }
        
        FeedbackContent(
            isSuccess = isSuccess,
            message = message,
            onBackClick = { navigator.pop() }
        )
    }
}

@Composable
private fun FeedbackContent(
    isSuccess: Boolean,
    message: String,
    onBackClick: () -> Unit
) {
    val icon: ImageVector
    val iconColor: Color
    val title: String
    
    if (isSuccess) {
        icon = Icons.Default.CheckCircle
        iconColor = Color(0xFF4CAF50) // Green
        title = "Sucesso!"
    } else {
        icon = Icons.Filled.Info
        iconColor = Color(0xFFF44336) // Red
        title = "Erro"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = title,
            style = ComandaAiTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ComandaAiTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Message
        Text(
            text = message,
            style = ComandaAiTheme.typography.bodyLarge,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Close button (only show for error cases)
        if (!isSuccess) {
            ComandaAiButton(
                text = "Voltar",
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Auto-close indicator for success
        if (isSuccess) {
            Text(
                text = "Voltando automaticamente...",
                style = ComandaAiTheme.typography.bodySmall,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}