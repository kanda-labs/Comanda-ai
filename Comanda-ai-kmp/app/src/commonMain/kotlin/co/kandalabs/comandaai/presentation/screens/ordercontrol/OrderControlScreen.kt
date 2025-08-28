package co.kandalabs.comandaai.presentation.screens.ordercontrol

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.presentation.designSystem.components.CommandaBadge
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.ErrorView
import comandaai.app.generated.resources.Res
import comandaai.app.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.domain.models.model.Order
import co.kandalabs.comandaai.domain.ItemOrder
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.domain.models.model.OrderStatus

public data class OrderControlScreen(val orderId: Int) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<OrderControlViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(orderId) {
            viewModel.setupOrderById(orderId)
        }

        OrderControlScreenContent(
            state = state,
            onBack = { navigator.pop() },
            onItemClick = { itemOrder ->
                if (itemOrder.count > 1) {
                    viewModel.toggleItemExpansion("${itemOrder.itemId}")
                } else if (state.userRole == "MANAGER") {
                    viewModel.showStatusModal(itemOrder)
                } else {
                    viewModel.updateItemStatus(itemOrder, getNextStatus(itemOrder.status))
                }
            },
            onItemBadgeClick = { itemOrder ->
                if (itemOrder.count > 1) {
                    viewModel.toggleItemExpansion("${itemOrder.itemId}")
                } else if (state.userRole == "MANAGER") {
                    viewModel.showStatusModal(itemOrder)
                } else {
                    viewModel.updateItemStatus(itemOrder, getNextStatus(itemOrder.status))
                }
            },
            onStatusSelected = { itemOrder, newStatus ->
                viewModel.updateItemStatus(itemOrder, newStatus)
            },
            onIndividualStatusSelected = { itemOrder, index, newStatus ->
                viewModel.updateIndividualItemStatus(itemOrder, index, newStatus)
            },
            onToggleExpansion = { itemId ->
                viewModel.toggleItemExpansion(itemId)
            },
            onIndividualItemClick = { itemOrder, index ->
                if (state.userRole == "MANAGER") {
                    viewModel.showIndividualItemStatusModal(itemOrder, index)
                } else {
                    viewModel.updateIndividualItemStatus(
                        itemOrder,
                        index,
                        getNextStatus(itemOrder.status)
                    )
                }
            },
            onDeliverAllItems = { itemOrder ->
                viewModel.deliverAllItems(itemOrder)
            },
            onDismissModal = { viewModel.hideStatusModal() }
        )
    }
}

@Composable
private fun OrderControlScreenContent(
    state: OrderControlState,
    onBack: () -> Unit,
    onItemClick: (ItemOrder) -> Unit,
    onItemBadgeClick: (ItemOrder) -> Unit,
    onStatusSelected: (ItemOrder, ItemStatus) -> Unit,
    onIndividualStatusSelected: (ItemOrder, Int, ItemStatus) -> Unit,
    onToggleExpansion: (String) -> Unit,
    onIndividualItemClick: (ItemOrder, Int) -> Unit,
    onDeliverAllItems: (ItemOrder) -> Unit,
    onDismissModal: () -> Unit
) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (state.isLoading) {
                ComandaAiLoadingView(
                    loadingImage = painterResource(Res.drawable.golden_loading)
                )
            } else if (state.error != null) {
                ErrorView(
                    error = state.error,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    ComandaAiTopAppBar(
                        "Pedido Nº ${state.order?.id ?: ""}",
                        onBackOrClose = onBack,
                        icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                    )

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                    // Status do pedido
                    state.order?.let { order ->
                        val (statusContainerColor, statusContentColor) = when (order.status) {
                            OrderStatus.DELIVERED -> Pair(
                                ComandaAiColors.Green500.value,
                                ComandaAiColors.OnSurface.value
                            )
                            OrderStatus.PENDING -> Pair(
                                ComandaAiColors.Yellow500.value,
                                ComandaAiColors.OnSurface.value
                            )
                            OrderStatus.CANCELED -> Pair(
                                ComandaAiColors.Error.value,
                                ComandaAiColors.OnError.value
                            )
                        }

                        val statusText = when (order.status) {
                            OrderStatus.DELIVERED -> "Entregue"
                            OrderStatus.PENDING -> "Pendente"
                            OrderStatus.CANCELED -> "Cancelado"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = ComandaAiSpacing.Medium.value),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CommandaBadge(
                                text = statusText,
                                containerColor = statusContainerColor,
                                contentColor = statusContentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                    Text(
                        "Itens do pedido:",
                        modifier = Modifier
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        color = ComandaAiColors.Gray700.value,
                        style = ComandaAiTypography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Small.value))

                    state.order?.let { order ->
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
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(horizontal = ComandaAiSpacing.Medium.value),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(order.items) { item ->
                                    OrderControlItemAccordion(
                                        item = item,
                                        userRole = state.userRole,
                                        isExpanded = state.expandedItems.contains("${item.itemId}"),
                                        individualItemStatuses = state.individualItemStatuses,
                                        onToggleExpansion = { onToggleExpansion("${item.itemId}") },
                                        onItemClick = onItemClick,
                                        onBadgeClick = onItemBadgeClick,
                                        onIndividualItemClick = onIndividualItemClick,
                                        onDeliverAllItems = onDeliverAllItems
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Status Modal for Managers
        if (state.showStatusModal) {
            when {
                state.selectedItem != null -> {
                    StatusSelectionModal(
                        item = state.selectedItem,
                        onDismiss = onDismissModal,
                        onStatusSelected = { newStatus ->
                            onStatusSelected(state.selectedItem, newStatus)
                        }
                    )
                }

                state.selectedIndividualItem != null -> {
                    val (item, index) = state.selectedIndividualItem
                    val key = "${item.itemId}_$index"
                    val currentIndividualStatus = state.individualItemStatuses[key] ?: item.status
                    IndividualItemStatusModal(
                        item = item,
                        individualIndex = index,
                        currentIndividualStatus = currentIndividualStatus,
                        onDismiss = onDismissModal,
                        onStatusSelected = { newStatus ->
                            onIndividualStatusSelected(item, index, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderControlItemAccordion(
    item: ItemOrder,
    userRole: String?,
    isExpanded: Boolean,
    individualItemStatuses: Map<String, ItemStatus>,
    onToggleExpansion: () -> Unit,
    onItemClick: (ItemOrder) -> Unit,
    onBadgeClick: (ItemOrder) -> Unit,
    onIndividualItemClick: (ItemOrder, Int) -> Unit,
    onDeliverAllItems: (ItemOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (item.status) {
        ItemStatus.DELIVERED -> Pair(
            ComandaAiColors.Green500.value,
            ComandaAiColors.OnSurface.value
        )

        ItemStatus.PENDING -> Pair(
            ComandaAiColors.Yellow500.value,
            ComandaAiColors.OnSurface.value
        )

        ItemStatus.CANCELED -> Pair(
            ComandaAiColors.Error.value,
            ComandaAiColors.OnError.value
        )
    }

    val statusText = when (item.status) {
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.PENDING -> "Pendente"
        ItemStatus.CANCELED -> "Cancelado"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Main item row
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

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommandaBadge(
                        text = statusText,
                        containerColor = containerColor,
                        contentColor = contentColor,
                        modifier = Modifier.clickable {
                            onBadgeClick(item)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (item.count > 1) {
                        IconButton(
                            onClick = onToggleExpansion,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = if (isExpanded) "Recolher" else "Expandir",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Expanded content
            if (isExpanded && item.count > 1) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Text(
                            text = "Itens individuais:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        repeat(item.count) { index ->
                            IndividualItemRow(
                                item = item,
                                index = index + 1,
                                individualStatus = individualItemStatuses["${item.itemId}_$index"] ?: ItemStatus.DELIVERED,
                                onItemClick = { onIndividualItemClick(item, index) }
                            )

                            if (index < item.count - 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onDeliverAllItems(item) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ComandaAiColors.Green500.value,
                                contentColor = ComandaAiColors.OnSurface.value
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Entregar todos os itens",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndividualItemRow(
    item: ItemOrder,
    index: Int,
    individualStatus: ItemStatus,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (individualStatus) {
        ItemStatus.DELIVERED -> Pair(
            ComandaAiColors.Green500.value.copy(alpha = 0.1f),
            ComandaAiColors.Green500.value
        )

        ItemStatus.PENDING -> Pair(
            ComandaAiColors.Blue500.value.copy(alpha = 0.1f),
            ComandaAiColors.Blue500.value
        )

        ItemStatus.CANCELED -> Pair(
            ComandaAiColors.Error.value.copy(alpha = 0.1f),
            ComandaAiColors.Error.value
        )
    }

    val statusText = when (individualStatus) {
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.PENDING -> "Pendente"
        ItemStatus.CANCELED -> "Cancelado"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${item.name} #$index",
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OrderControlItem(
    item: ItemOrder,
    userRole: String?,
    onItemClick: (ItemOrder) -> Unit,
    onBadgeClick: (ItemOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (item.status) {
        ItemStatus.DELIVERED -> Pair(
            ComandaAiColors.Green500.value,
            ComandaAiColors.OnSurface.value
        )

        ItemStatus.PENDING -> Pair(
            ComandaAiColors.Yellow500.value,
            ComandaAiColors.OnSurface.value
        )

        ItemStatus.CANCELED -> Pair(
            ComandaAiColors.Error.value,
            ComandaAiColors.OnError.value
        )
    }

    val statusText = when (item.status) {
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.PENDING -> "Pendente"
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
                contentColor = contentColor,
                modifier = Modifier.clickable {
                    onBadgeClick(item)
                }
            )
        }
    }
}

@Composable
private fun StatusSelectionModal(
    item: ItemOrder,
    onDismiss: () -> Unit,
    onStatusSelected: (ItemStatus) -> Unit
) {
    // Filtra os status disponíveis excluindo o status atual
    val availableStatuses = ItemStatus.values().filter { it != item.status }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value)
                .clickable { },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(ComandaAiSpacing.Medium.value)
            ) {
                Text(
                    text = "Alterar Status",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = ComandaAiSpacing.Medium.value)
                )
                
                Text(
                    text = "Item: ${item.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                val currentStatusText = when (item.status) {
                    ItemStatus.DELIVERED -> "Entregue"
                    ItemStatus.PENDING -> "Pendente"
                    ItemStatus.CANCELED -> "Cancelado"
                }

                Text(
                    text = "Status atual: $currentStatusText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecione o novo status:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                availableStatuses.forEach { status ->
                    val statusText = when (status) {
                        ItemStatus.DELIVERED -> "Entregue"
                        ItemStatus.PENDING -> "Pendente"
                        ItemStatus.CANCELED -> "Cancelado"
                    }

                    val (bgColor, textColor) = when (status) {
                        ItemStatus.DELIVERED -> Pair(
                            ComandaAiColors.Green500.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )

                        ItemStatus.PENDING -> Pair(
                            ComandaAiColors.Yellow500.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )

                        ItemStatus.CANCELED -> Pair(
                            ComandaAiColors.Error.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )
                        
                        else -> Pair(
                            ComandaAiColors.Gray200.value,
                            ComandaAiColors.OnSurface.value
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                onStatusSelected(status)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = bgColor
                        )
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ComandaAiColors.OnSurface.value,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComandaAiColors.Gray200.value,
                        contentColor = ComandaAiColors.OnSurface.value
                    )
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

@Composable
private fun IndividualItemStatusModal(
    item: ItemOrder,
    individualIndex: Int,
    currentIndividualStatus: ItemStatus,
    onDismiss: () -> Unit,
    onStatusSelected: (ItemStatus) -> Unit
) {
    // Filtra os status disponíveis excluindo o status atual individual
    val availableStatuses = ItemStatus.values().filter { it != currentIndividualStatus }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComandaAiSpacing.Medium.value)
                .clickable { },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(ComandaAiSpacing.Medium.value)
            ) {
                Text(
                    text = "Alterar Status Individual",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = ComandaAiSpacing.Medium.value)
                )
                
                Text(
                    text = "Item: ${item.name} #${individualIndex + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                val currentStatusText = when (currentIndividualStatus) {
                    ItemStatus.DELIVERED -> "Entregue"
                    ItemStatus.PENDING -> "Pendente"
                    ItemStatus.CANCELED -> "Cancelado"
                }

                Text(
                    text = "Status atual: $currentStatusText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Selecione o novo status:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                availableStatuses.forEach { status ->
                    val statusText = when (status) {
                        ItemStatus.DELIVERED -> "Entregue"
                        ItemStatus.PENDING -> "Pendente"
                        ItemStatus.CANCELED -> "Cancelado"
                    }

                    val (bgColor, textColor) = when (status) {
                        ItemStatus.DELIVERED -> Pair(
                            ComandaAiColors.Green500.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )

                        ItemStatus.PENDING -> Pair(
                            ComandaAiColors.Yellow500.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )

                        ItemStatus.CANCELED -> Pair(
                            ComandaAiColors.Error.value.copy(alpha = 0.1f),
                            ComandaAiColors.OnSurface.value
                        )
                        
                        else -> Pair(
                            ComandaAiColors.Gray200.value,
                            ComandaAiColors.OnSurface.value
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                onStatusSelected(status)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = bgColor
                        )
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ComandaAiColors.OnSurface.value,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ComandaAiColors.Gray200.value,
                        contentColor = ComandaAiColors.OnSurface.value
                    )
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}

private fun getNextStatus(currentStatus: ItemStatus): ItemStatus {
    return when (currentStatus) {
        ItemStatus.PENDING -> ItemStatus.DELIVERED
        ItemStatus.DELIVERED -> ItemStatus.PENDING
        ItemStatus.CANCELED -> ItemStatus.PENDING
    }
}