package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ConnectionStatusBullet(
    isConnected: Boolean,
    isReconnecting: Boolean,
    onReconnectClick: (() -> Unit)? = null
) {
    val color = when {
        isReconnecting -> ComandaAiTheme.colorScheme.primary
        isConnected -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
        else -> androidx.compose.ui.graphics.Color(0xFFFF5252) // Red
    }
    
    val contentDescription = when {
        isReconnecting -> "Reconectando..."
        isConnected -> "Conectado"
        else -> "Desconectado - Toque para reconectar"
    }
    
    Box(
        modifier = Modifier
            .size(10.dp)
            .then(
                if (onReconnectClick != null) {
                    Modifier.clickable { onReconnectClick() }
                } else {
                    Modifier
                }
            )
    ) {
        if (isReconnecting) {
            CircularProgressIndicator(
                modifier = Modifier.size(10.dp),
                strokeWidth = 1.dp,
                color = color
            )
        } else {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = color
            ) {}
        }
    }
}