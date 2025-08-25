package co.kandalabs.comandaai.kitchen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.presentation.components.OrderCard
import co.kandalabs.comandaai.kitchen.presentation.components.UserAvatar
import co.kandalabs.comandaai.kitchen.presentation.components.UserProfileModal
import kotlinx.coroutines.launch
import org.kodein.di.instance

class KitchenScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<KitchenViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()

        var showUserModal by remember { mutableStateOf(false) }
        var userSession by remember { mutableStateOf<UserSession?>(null) }
        var selectedTab by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            userSession = viewModel.getUserSession()
        }

        KitchenScreenContent(
            state = state,
            userSession = userSession,
            showUserModal = showUserModal,
            selectedTab = selectedTab,
            onTabChange = { selectedTab = it },
            onRefresh = viewModel::refreshOrders,
            onReconnect = viewModel::reconnectSSE,
            onItemStatusChange = viewModel::updateItemStatus,
            onMarkAsDelivered = viewModel::markOrderAsDelivered,
            onMarkItemAsDelivered = viewModel::markItemAsDelivered,
            onErrorDismiss = viewModel::clearError,
            onUserAvatarClick = {
                scope.launch {
                    userSession = viewModel.getUserSession()
                    showUserModal = true
                }
            },
            onDismissUserModal = { showUserModal = false },
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
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onRefresh: () -> Unit,
    onReconnect: () -> Unit,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onMarkAsDelivered: (Int) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit,
    onErrorDismiss: () -> Unit,
    onUserAvatarClick: () -> Unit,
    onDismissUserModal: () -> Unit,
    onLogout: () -> Unit
) {
    // Preservar estado do scroll
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        UserAvatar(
                            userName = userSession?.userName,
                            onClick = onUserAvatarClick
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text("Pedidos ativos", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${state.orders.size} pedidos ativos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
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
                                contentDescription = "Atualizar pedidos"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Controle") },
                    selected = selectedTab == 0,
                    onClick = { onTabChange(0) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Panorama") },
                    selected = selectedTab == 1,
                    onClick = { onTabChange(1) }
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
                Snackbar(
                    modifier = Modifier.padding(vertical = 8.dp),
                    action = {
                        TextButton(onClick = onErrorDismiss) { Text("OK") }
                    }
                ) { Text(error) }
            }

            when (selectedTab) {
                0 -> OrderControlTab(
                    state = state,
                    listState = listState,
                    onItemStatusChange = onItemStatusChange,
                    onMarkAsDelivered = onMarkAsDelivered,
                    onMarkItemAsDelivered = onMarkItemAsDelivered
                )

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
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Carregando pedidos...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = "Nenhum pedido ativo",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Novos pedidos aparecerão aqui automaticamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun OrderControlTab(
    state: KitchenScreenState,
    listState: LazyListState,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onMarkAsDelivered: (Int) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit
) {
    when {
        state.isLoading -> LoadingState()
        state.orders.isEmpty() -> EmptyState()
        else -> LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(
                items = state.orders,
                key = { order -> order.id }
            ) { order ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    OrderCard(
                        order = order,
                        onItemStatusChange = { itemId, unitIndex, status ->
                            onItemStatusChange(order.id, itemId, unitIndex, status)
                        },
                        onMarkAsDelivered = onMarkAsDelivered,
                        onMarkItemAsDelivered = onMarkItemAsDelivered
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderOverviewTab(state: KitchenScreenState) {
    var selectedCategories by remember { mutableStateOf(setOf(ItemCategory.SKEWER)) }

    val itemSummary = remember(state.orders, selectedCategories) {
        state.orders.flatMap { order ->
            order.items.filter { item ->
                selectedCategories.contains(item.category)
            }
        }.groupBy { it.itemId }
            .mapValues { (_, items) ->
                // Contar apenas itens que não foram entregues (excluir DELIVERED)
                items.sumOf { item ->
                    item.unitStatuses.count { unitStatus ->
                        unitStatus.status != ItemStatus.DELIVERED
                    }
                }
            }
            .mapKeys { (itemId, _) ->
                state.orders.flatMap { it.items }
                    .first { it.itemId == itemId }
            }
            .filter { (_, count) -> count > 0 } // Remover itens com contagem zero
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Filter - Horizontal Badges
        val availableCategories = listOf(
            ItemCategory.SKEWER to "Espetinhos",
            ItemCategory.SNACK to "Petiscos",
            ItemCategory.DRINK to "Bebidas",
            ItemCategory.PROMOTIONAL to "Promo"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableCategories) { (categoryEnum, categoryName) ->
                val isSelected = selectedCategories.contains(categoryEnum)
                Surface(
                    modifier = Modifier.clickable {
                        selectedCategories = if (isSelected) {
                            selectedCategories - categoryEnum
                        } else {
                            selectedCategories + categoryEnum
                        }
                    },
                    shape = MaterialTheme.shapes.small,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = if (isSelected) null else BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(
                        text = categoryName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        // Items Summary
        if (itemSummary.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "Nenhum item encontrado",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Selecione outras categorias para ver o resumo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
            ) {
                items(itemSummary.toList()) { (item, totalQuantity) ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = totalQuantity.toString(),
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 6.dp
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
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
private fun ConnectionStatusBullet(
    isConnected: Boolean,
    isReconnecting: Boolean,
    onReconnectClick: (() -> Unit)? = null
) {
    val color = when {
        isReconnecting -> MaterialTheme.colorScheme.primary
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