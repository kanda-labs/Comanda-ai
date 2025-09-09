package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTextButton
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun UserProfileModal(
    isVisible: Boolean,
    userSession: UserSession?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val offsetY = remember { Animatable(0f) }
        var isDragging by remember { mutableStateOf(false) }
        
        LaunchedEffect(isVisible) {
            if (!isVisible) {
                offsetY.animateTo(0f, tween(300))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clickable(onClick = onDismiss)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) { } // Prevent click through
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = {
                                    isDragging = false
                                    scope.launch {
                                        val dismissThreshold = 150.dp.toPx()
                                        if (offsetY.value > dismissThreshold) {
                                            onDismiss()
                                        } else {
                                            offsetY.animateTo(0f, tween(200))
                                        }
                                    }
                                }
                            ) { _, dragAmount ->
                                scope.launch {
                                    val newOffset = (offsetY.value + dragAmount.y).coerceAtLeast(0f)
                                    offsetY.snapTo(newOffset)
                                }
                            }
                        },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(ComandaAiSpacing.Large.value),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drag handle
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Large.value))
                        
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userSession?.userName?.isNotEmpty() == true) {
                                Text(
                                    text = userSession.userName.first().uppercaseChar().toString(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar do usuário",
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                        
                        // User Name
                        Text(
                            text = userSession?.userName ?: "Usuário",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))
                        
                        // User Role
                        Text(
                            text = userSession?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Função",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))
                        
                        // Logout Button
                        ComandaAiButton(
                            text = "Logout",
                            onClick = {
                                onLogout()
                                onDismiss()
                            },
                            variant = ComandaAiButtonVariant.Destructive,
                            modifier = Modifier.height(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                        
                        // Cancel Button
                        ComandaAiTextButton(
                            text = "Cancelar",
                            onClick = onDismiss
                        )
                    }
                }
            }
        }
    }
}