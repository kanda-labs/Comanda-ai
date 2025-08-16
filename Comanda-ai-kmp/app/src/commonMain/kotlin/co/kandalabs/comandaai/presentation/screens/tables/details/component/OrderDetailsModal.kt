package co.kandalabs.comandaai.presentation.screens.tables.details.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.presentation.designSystem.components.CommandaBadge
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import kandalabs.commander.domain.model.Order
import kandalabs.commander.domain.model.ItemOrder
import kandalabs.commander.domain.model.ItemStatus

@Composable
internal fun OrderDetailsModal(
    isVisible: Boolean,
    order: Order,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val offsetY = remember { Animatable(0f) }
        var isDragging by remember { mutableStateOf(false) }
        
        LaunchedEffect(isVisible) {
            if (!isVisible) {
                offsetY.snapTo(0f)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                )
            ) {
                Surface(
                    modifier = modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .offset { IntOffset(0, offsetY.value.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = {
                                    isDragging = false
                                    if (offsetY.value > 150) {
                                        onDismiss()
                                    } else {
                                        scope.launch {
                                            offsetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(300)
                                            )
                                        }
                                    }
                                }
                            ) { _, dragAmount ->
                                val newOffset = offsetY.value + dragAmount.y
                                if (newOffset >= 0) {
                                    scope.launch {
                                        offsetY.snapTo(newOffset)
                                    }
                                }
                            }
                        }
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .height(24.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { isDragging = true },
                                        onDragEnd = {
                                            isDragging = false
                                            if (offsetY.value > 150) {
                                                onDismiss()
                                            } else {
                                                scope.launch {
                                                    offsetY.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(300)
                                                    )
                                                }
                                            }
                                        }
                                    ) { _, dragAmount ->
                                        val newOffset = offsetY.value + dragAmount.y
                                        if (newOffset >= 0) {
                                            scope.launch {
                                                offsetY.snapTo(newOffset)
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Pedido Nº ${order.id}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        
                        if (order.items.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum item neste pedido",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Itens do pedido:",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(order.items) { item ->
                                        OrderDetailsItem(
                                            item = item
                                        )
                                    }
                                }
                            }
                        }
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 4.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                ComandaAiButton(
                                    text = "Voltar",
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderDetailsItem(
    item: ItemOrder,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (item.status) {
        ItemStatus.GRANTED -> Pair(
            ComandaAiColors.Green500.value,
            ComandaAiColors.OnSurface.value
        )
        ItemStatus.OPEN -> Pair(
            ComandaAiColors.Blue500.value,
            ComandaAiColors.OnSurface.value
        )
        ItemStatus.CANCELED -> Pair(
            ComandaAiColors.Error.value,
            ComandaAiColors.OnError.value
        )
    }

    val statusText = when (item.status) {
        ItemStatus.GRANTED -> "Atendido"
        ItemStatus.OPEN -> "Pendente"
        ItemStatus.CANCELED -> "Cancelado"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Quantidade: ${item.count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                item.observation?.let { observation ->
                    Text(
                        text = "Obs: $observation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(ComandaAiSpacing.Small.value))
            
            CommandaBadge(
                text = statusText,
                containerColor = containerColor,
                contentColor = contentColor
            )
        }
    }
}