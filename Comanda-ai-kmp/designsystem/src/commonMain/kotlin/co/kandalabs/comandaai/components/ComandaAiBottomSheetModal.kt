package co.kandalabs.comandaai.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class ComandaAiModalPresentationMode {
    Dynamic,    // Altura automática baseada no conteúdo (padrão)
    Small,      // 30% da tela
    Mid,        // 50% da tela
    Full        // 100% da tela (altura máxima)
}

@Composable
fun ComandaAiBottomSheetModal(
    isVisible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    presentationMode: ComandaAiModalPresentationMode = ComandaAiModalPresentationMode.Dynamic,
    dismissOnBackgroundClick: Boolean = true,
    dismissOnDrag: Boolean = true,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (isVisible) {
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
                .background(Color.Black.copy(alpha = 0.6f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clickable(enabled = dismissOnBackgroundClick) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(300)
                )
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .let { surfaceModifier ->
                            when (presentationMode) {
                                ComandaAiModalPresentationMode.Dynamic -> surfaceModifier.wrapContentHeight()
                                ComandaAiModalPresentationMode.Small -> surfaceModifier.fillMaxHeight(0.3f)
                                ComandaAiModalPresentationMode.Mid -> surfaceModifier.fillMaxHeight(0.5f)
                                ComandaAiModalPresentationMode.Full -> surfaceModifier.fillMaxHeight(1.0f)
                            }
                        }
                        .clickable(enabled = false) { }
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .let { surfaceModifier ->
                            if (dismissOnDrag) {
                                surfaceModifier.pointerInput(Unit) {
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
                                }
                            } else {
                                surfaceModifier
                            }
                        },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = ComandaAiTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    // Para modo Dynamic, usamos layout normal
                    if (presentationMode == ComandaAiModalPresentationMode.Dynamic) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            ModalHeader(
                                title = title,
                                dismissOnDrag = dismissOnDrag
                            )
                            
                            content()
                            
                            // Actions no final do conteúdo para modo Dynamic
                            actions?.let { actionsContent ->
                                ComandaAiBottomSheetModalActions(content = actionsContent)
                            }
                        }
                    } else {
                        // Para modos de tamanho fixo, garantimos que actions ficam sempre no bottom
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = if (actions != null) 0.dp else 16.dp)
                            ) {
                                ModalHeader(
                                    title = title,
                                    dismissOnDrag = dismissOnDrag
                                )
                                
                                content()
                            }
                            
                            // Actions sempre no bottom para modos de tamanho fixo
                            actions?.let { actionsContent ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = ComandaAiTheme.colorScheme.surface
                                ) {
                                    ComandaAiBottomSheetModalActions(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        content = actionsContent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalHeader(
    title: String,
    dismissOnDrag: Boolean
) {
    Column {
        // Drag indicator (only show if drag to dismiss is enabled)
        if (dismissOnDrag) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = ComandaAiTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Title
        Text(
            text = title,
            style = ComandaAiTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = ComandaAiTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ComandaAiBottomSheetModalActions(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}