package co.kandalabs.comandaai.kitchen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.sdk.session.UserSession
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.presentation.components.ConnectionStatusBullet
import co.kandalabs.comandaai.kitchen.presentation.components.OrderControlTab
import co.kandalabs.comandaai.kitchen.presentation.components.OrderFilterToggle
import co.kandalabs.comandaai.kitchen.presentation.components.OrderOverviewTab
import co.kandalabs.comandaai.kitchen.presentation.components.UserAvatar
import co.kandalabs.comandaai.kitchen.presentation.components.UserProfileModal
import kotlinx.coroutines.launch

object KitchenScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<KitchenViewModel>()
        val state by viewModel.state.collectAsState()
        
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()

        var showUserModal by remember { mutableStateOf(false) }
        var showDeliveryConfirmationModal by remember { mutableStateOf(false) }
        var orderToDeliver by remember { mutableStateOf<KitchenOrder?>(null) }
        var userSession by remember { mutableStateOf<UserSession?>(null) }
        var selectedTab by remember { mutableStateOf(0) }
        var removingOrderIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

        LaunchedEffect(Unit) {
            userSession = viewModel.getUserSession()
        }
        
        KitchenScreenContent(
            state = state,
            userSession = userSession,
            showUserModal = showUserModal,
            showDeliveryConfirmationModal = showDeliveryConfirmationModal,
            orderToDeliver = orderToDeliver,
            selectedTab = selectedTab,
            removingOrderIds = removingOrderIds,
            onTabChange = { selectedTab = it },
            onRefresh = viewModel::refreshOrders,
            onReconnect = viewModel::reconnectSSE,
            onItemStatusChange = viewModel::updateItemStatus,
            onMarkItemAsDelivered = viewModel::markItemAsDelivered,
            onErrorDismiss = viewModel::clearError,
            onFilterChange = viewModel::switchOrderFilter,
            onUserAvatarClick = {
                scope.launch {
                    userSession = viewModel.getUserSession()
                    showUserModal = true
                }
            },
            onDismissUserModal = { showUserModal = false },
            onShowDeliveryConfirmation = { order ->
                orderToDeliver = order
                showDeliveryConfirmationModal = true
            },
            onDismissDeliveryConfirmation = { 
                showDeliveryConfirmationModal = false
                orderToDeliver = null
            },
            onConfirmDelivery = { orderId ->
                removingOrderIds = removingOrderIds + orderId
                showDeliveryConfirmationModal = false
                orderToDeliver = null
            },
            onOrderRemovalComplete = { orderId ->
                removingOrderIds = removingOrderIds - orderId
                viewModel.markOrderAsDelivered(orderId)
            },
            onLogout = {
                viewModel.logout()
                navigator?.replaceAll(AuthModule.getLoginScreen())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitchenScreenContent(
    state: KitchenScreenState,
    userSession: UserSession?,
    showUserModal: Boolean,
    showDeliveryConfirmationModal: Boolean,
    orderToDeliver: KitchenOrder?,
    selectedTab: Int,
    removingOrderIds: Set<Int>,
    onTabChange: (Int) -> Unit,
    onRefresh: () -> Unit,
    onReconnect: () -> Unit,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit,
    onErrorDismiss: () -> Unit,
    onFilterChange: (OrderFilter) -> Unit,
    onUserAvatarClick: () -> Unit,
    onDismissUserModal: () -> Unit,
    onShowDeliveryConfirmation: (KitchenOrder) -> Unit,
    onDismissDeliveryConfirmation: () -> Unit,
    onConfirmDelivery: (Int) -> Unit,
    onOrderRemovalComplete: (Int) -> Unit,
    onLogout: () -> Unit
) {
    // Preservar estado do scroll
    val listState = rememberLazyListState()
    Scaffold(
        containerColor = ComandaAiTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ComandaAiTheme.colorScheme.surfaceVariant)
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    userName = userSession?.userName,
                    onClick = onUserAvatarClick
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    val titleText = when (state.currentFilter) {
                        OrderFilter.ACTIVE -> "Pedidos ativos"
                        OrderFilter.DELIVERED -> "Pedidos entregues"
                    }
                    val countText = when (state.currentFilter) {
                        OrderFilter.ACTIVE -> "${state.activeOrders.size} pedidos ativos"
                        OrderFilter.DELIVERED -> "${state.deliveredOrders.size} pedidos entregues"
                    }

                    Text(
                        text = titleText,
                        style = ComandaAiTheme.typography.titleLarge,
                        color = ComandaAiTheme.colorScheme.onSurface
                    )
                    Text(
                        text = countText,
                        style = ComandaAiTheme.typography.bodySmall,
                        color = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Connection Status Bullet
                    ConnectionStatusBullet(
                        isConnected = state.isConnected,
                        isReconnecting = state.isReconnecting,
                        onReconnectClick = if (!state.isConnected && !state.isReconnecting) onReconnect else null
                    )

                    // Refresh Button
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar pedidos",
                            tint = ComandaAiTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ComandaAiTheme.colorScheme.surfaceVariant
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Controle") },
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ComandaAiTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = ComandaAiTheme.colorScheme.onSurface,
                        indicatorColor = ComandaAiTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Panorama") },
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ComandaAiTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = ComandaAiTheme.colorScheme.onSurface,
                        indicatorColor = ComandaAiTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = ComandaAiTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = ComandaAiTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            
            // Erro
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComandaAiTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = ComandaAiTheme.typography.bodyMedium,
                            color = ComandaAiTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ComandaAiButton(
                            text = "OK",
                            onClick = onErrorDismiss,
                            variant = ComandaAiButtonVariant.Secondary
                        )
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // Order Filter Toggle - apenas na aba Controle
                    OrderFilterToggle(
                        currentFilter = state.currentFilter,
                        onFilterChange = onFilterChange,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    OrderControlTab(
                        state = state,
                        listState = listState,
                        onItemStatusChange = onItemStatusChange,
                        onMarkItemAsDelivered = onMarkItemAsDelivered,
                        onShowDeliveryConfirmation = onShowDeliveryConfirmation,
                        removingOrderIds = removingOrderIds,
                        onOrderRemovalComplete = onOrderRemovalComplete
                    )
                }

                1 -> OrderOverviewTab(state = state)
            }
        }
    }

    // User Profile Modal - Moved outside of Scaffold to fix z-index issue
    UserProfileModal(
        isVisible = showUserModal,
        userName = userSession?.userName,
        userRole = userSession?.role?.name,
        onDismiss = onDismissUserModal,
        onLogout = onLogout
    )
    
    // Delivery Confirmation Modal - Moved outside of Scaffold to fix z-index issue
    orderToDeliver?.let { order ->
        ComandaAiBottomSheetModal(
            isVisible = showDeliveryConfirmationModal,
            title = "Confirmar entrega",
            onDismiss = onDismissDeliveryConfirmation,
            actions = {
                ComandaAiButton(
                    text = "Cancelar",
                    onClick = onDismissDeliveryConfirmation,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ComandaAiButtonVariant.Secondary
                )

                ComandaAiButton(
                    text = "Confirmar entrega",
                    onClick = { onConfirmDelivery(order.id) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ComandaAiButtonVariant.Primary
                )
            }
        ) {
            Text(
                text = "Deseja marcar este pedido como entregue?",
                style = ComandaAiTheme.typography.bodyLarge,
                color = ComandaAiTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mesa ${order.tableNumber} • ${order.userName}",
                style = ComandaAiTheme.typography.bodyMedium,
                color = ComandaAiTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

