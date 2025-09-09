package co.kandalabs.comandaai.features.attendance.presentation.screens.ordercontrol

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.domain.ItemOrder
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.features.attendance.domain.models.model.OrderStatus
import co.kandalabs.comandaai.components.CommandaBadge
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource

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
                    // Para usuários não-gerentes, permitir ciclar status apenas se não estiver cancelado
                    if (itemOrder.status != ItemStatus.CANCELED) {
                        viewModel.updateItemStatus(itemOrder, getNextStatus(itemOrder.status))
                    }
                }
            },
            onItemBadgeClick = { itemOrder ->
                if (itemOrder.count > 1) {
                    viewModel.toggleItemExpansion("${itemOrder.itemId}")
                } else if (state.userRole == "MANAGER") {
                    viewModel.showStatusModal(itemOrder)
                } else {
                    // Para usuários não-gerentes, permitir ciclar status apenas se não estiver cancelado
                    if (itemOrder.status != ItemStatus.CANCELED) {
                        viewModel.updateItemStatus(itemOrder, getNextStatus(itemOrder.status))
                    }
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
                val key = "${itemOrder.itemId}_$index"
                val individualStatus = state.individualItemStatuses[key] ?: itemOrder.status
                if (state.userRole == "MANAGER") {
                    viewModel.showIndividualItemStatusModal(itemOrder, index)
                } else {
                    // Para usuários não-gerentes, permitir ciclar status apenas se não estiver cancelado
                    if (individualStatus != ItemStatus.CANCELED) {
                        viewModel.updateIndividualItemStatus(
                            itemOrder,
                            index,
                            getNextStatus(individualStatus)
                        )
                    }
                }
            },
            onDeliverAllItems = { itemOrder ->
                viewModel.deliverAllItems(itemOrder)
            },
            onDeliverAllOrderItems = {
                viewModel.showDeliverAllConfirmationModal()
            },
            onConfirmDeliverAllItems = {
                viewModel.deliverAllOrderItems()
            },
            onCancelOrder = {
                viewModel.showCancelOrderConfirmationModal()
            },
            onConfirmCancelOrder = {
                viewModel.cancelOrder()
            },
            onDismissModal = { viewModel.hideStatusModal() },
            onDismissDeliverAllModal = { viewModel.hideDeliverAllConfirmationModal() },
            onDismissCancelOrderModal = { viewModel.hideCancelOrderConfirmationModal() }
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
    onDeliverAllOrderItems: () -> Unit,
    onConfirmDeliverAllItems: () -> Unit,
    onCancelOrder: () -> Unit,
    onConfirmCancelOrder: () -> Unit,
    onDismissModal: () -> Unit,
    onDismissDeliverAllModal: () -> Unit,
    onDismissCancelOrderModal: () -> Unit
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
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

                                Text(
                                    "Criado por: ${state.createdBy}",
                                    color = ComandaAiColors.Surface.value,
                                    style = ComandaAiTypography.titleMedium
                                )

                                Spacer(Modifier.weight(1F))

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
                                        .fillMaxWidth()
                                        .weight(1f),
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
                                    contentPadding = PaddingValues(
                                        start = ComandaAiSpacing.Medium.value,
                                        end = ComandaAiSpacing.Medium.value,
                                        bottom = 100.dp // Espaço para os botões fixos
                                    ),
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
                    
                    // Botões fixos na parte inferior da tela
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(ComandaAiSpacing.Medium.value),
                        verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                    ) {
                        state.order?.let { order ->
                            if (order.status != OrderStatus.CANCELED) {
                                // Mostrar botão "Entregar Todos os Itens" se há itens que não estão entregues e não estão cancelados
                                if (order.items.any { it.status != ItemStatus.DELIVERED && it.status != ItemStatus.CANCELED }) {
                                    ComandaAiButton(
                                        text = "Entregar Todos os Itens",
                                        onClick = onDeliverAllOrderItems,
                                        variant = ComandaAiButtonVariant.Primary
                                    )
                                }
                                
                                ComandaAiButton(
                                    text = "Cancelar Pedido",
                                    onClick = onCancelOrder,
                                    variant = ComandaAiButtonVariant.Destructive
                                )
                            }
                        }
                        
                        ComandaAiButton(
                            text = "Voltar",
                            onClick = onBack,
                            variant = ComandaAiButtonVariant.Secondary
                        )
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

        // Deliver All Items Confirmation Modal
        ComandaAiBottomSheetModal(
            isVisible = state.showDeliverAllConfirmationModal,
            title = "Confirmar Entrega",
            onDismiss = onDismissDeliverAllModal,
            actions = {
                ComandaAiButton(
                    text = "Confirmar",
                    onClick = onConfirmDeliverAllItems,
                    variant = ComandaAiButtonVariant.Primary
                )
                
                ComandaAiButton(
                    text = "Cancelar",
                    onClick = onDismissDeliverAllModal,
                    variant = ComandaAiButtonVariant.Secondary
                )
            }
        ) {
            Text(
                text = "Tem certeza que deseja entregar todos os itens deste pedido?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Esta ação não pode ser desfeita.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        // Cancel Order Confirmation Modal
        ComandaAiBottomSheetModal(
            isVisible = state.showCancelOrderConfirmationModal,
            title = "Cancelar Pedido",
            onDismiss = onDismissCancelOrderModal,
            actions = {
                ComandaAiButton(
                    text = "Sim, Cancelar",
                    onClick = onConfirmCancelOrder,
                    variant = ComandaAiButtonVariant.Destructive
                )
                
                ComandaAiButton(
                    text = "Não Cancelar",
                    onClick = onDismissCancelOrderModal,
                    variant = ComandaAiButtonVariant.Secondary
                )
            }
        ) {
            Text(
                text = "Tem certeza que deseja cancelar este pedido?",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Todos os itens serão cancelados e esta ação não pode ser desfeita.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
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

                        ComandaAiButton(
                            text = "Entregar todos os itens",
                            onClick = { onDeliverAllItems(item) },
                            variant = ComandaAiButtonVariant.Primary
                        )
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

    val currentStatusText = when (item.status) {
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.PENDING -> "Pendente"
        ItemStatus.CANCELED -> "Cancelado"
    }

    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Alterar Status",
        onDismiss = onDismiss,
        actions = {
            ComandaAiButton(
                text = "Cancelar",
                onClick = onDismiss,
                variant = ComandaAiButtonVariant.Secondary
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Item: ${item.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Status atual: $currentStatusText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Selecione o novo status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableStatuses.forEach { status ->
                    val statusText = when (status) {
                        ItemStatus.DELIVERED -> "Entregue"
                        ItemStatus.PENDING -> "Pendente"
                        ItemStatus.CANCELED -> "Cancelado"
                    }

                    val (bgColor, textColor) = when (status) {
                        ItemStatus.DELIVERED -> Pair(
                            ComandaAiColors.Green500.value.copy(alpha = 0.1f),
                            ComandaAiColors.Green500.value
                        )

                        ItemStatus.PENDING -> Pair(
                            ComandaAiColors.Yellow500.value.copy(alpha = 0.1f),
                            ComandaAiColors.Yellow500.value
                        )

                        ItemStatus.CANCELED -> Pair(
                            ComandaAiColors.Error.value.copy(alpha = 0.1f),
                            ComandaAiColors.Error.value
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
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

    val currentStatusText = when (currentIndividualStatus) {
        ItemStatus.DELIVERED -> "Entregue"
        ItemStatus.PENDING -> "Pendente"
        ItemStatus.CANCELED -> "Cancelado"
    }

    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Alterar Status Individual",
        onDismiss = onDismiss,
        actions = {
            ComandaAiButton(
                text = "Cancelar",
                onClick = onDismiss,
                variant = ComandaAiButtonVariant.Secondary
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Item: ${item.name} #${individualIndex + 1}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Status atual: $currentStatusText",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Selecione o novo status:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableStatuses.forEach { status ->
                    val statusText = when (status) {
                        ItemStatus.DELIVERED -> "Entregue"
                        ItemStatus.PENDING -> "Pendente"
                        ItemStatus.CANCELED -> "Cancelado"
                    }

                    val (bgColor, textColor) = when (status) {
                        ItemStatus.DELIVERED -> Pair(
                            ComandaAiColors.Green500.value.copy(alpha = 0.1f),
                            ComandaAiColors.Green500.value
                        )

                        ItemStatus.PENDING -> Pair(
                            ComandaAiColors.Yellow500.value.copy(alpha = 0.1f),
                            ComandaAiColors.Yellow500.value
                        )

                        ItemStatus.CANCELED -> Pair(
                            ComandaAiColors.Error.value.copy(alpha = 0.1f),
                            ComandaAiColors.Error.value
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
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