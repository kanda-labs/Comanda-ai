package co.kandalabs.comandaai.presentation.screens.ordersline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.components.UserAvatar
import co.kandalabs.comandaai.core.session.UserSession
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.presentation.screens.itemsSelection.components.LoadingView
import co.kandalabs.comandaai.presentation.screens.ordersline.components.OrderCard
import co.kandalabs.comandaai.presentation.screens.ordersline.components.OrdersSummaryBar
import co.kandalabs.comandaai.presentation.screens.tables.listing.components.UserProfileModal
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import kotlinx.coroutines.launch

public object OrdersLineScreen : Screen {
    
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<OrdersLineViewModel>()
        val state by viewModel.state.collectAsState()
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()
        
        var showUserModal by remember { mutableStateOf(false) }
        var userSession by remember { mutableStateOf<UserSession?>(null) }
        
        LaunchedEffect(Unit) {
            userSession = viewModel.getUserSession()
        }
        
        OrdersLineScreenContent(
            state = state,
            userSession = userSession,
            showUserModal = showUserModal,
            onRefresh = { viewModel.refreshOrders() },
            onMarkOrderComplete = { orderId -> viewModel.markOrderAsCompleted(orderId) },
            onMarkItemComplete = { orderId, itemId -> viewModel.markItemAsCompleted(orderId, itemId) },
            onSelectOrder = { orderId -> viewModel.selectOrder(orderId) },
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

@Composable
private fun OrdersLineScreenContent(
    state: OrdersLineScreenState,
    userSession: UserSession?,
    showUserModal: Boolean,
    onRefresh: () -> Unit,
    onMarkOrderComplete: (Int) -> Unit,
    onMarkItemComplete: (Int, Int) -> Unit,
    onSelectOrder: (Int?) -> Unit,
    onUserAvatarClick: () -> Unit,
    onDismissUserModal: () -> Unit,
    onLogout: () -> Unit
) {
    MaterialTheme {
        if (state.isLoading && !state.hasOrders) {
            LoadingView()
        } else if (state.error != null && !state.hasOrders) {
            ErrorView(
                error = state.error,
                retry = onRefresh
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                ComandaAiTopAppBar(
                    title = state.title,
                    actions = {
                        UserAvatar(
                            userName = userSession?.userName,
                            onClick = onUserAvatarClick
                        )
                    }
                )
                
                // Summary bar
                OrdersSummaryBar(
                    pendingCount = state.pendingOrders.size,
                    completedCount = state.completedOrders.size
                )
                
                // Error banner if SSE connection failed
                state.error?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ComandaAiSpacing.Medium.value),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error.message ?: "Erro de conexão",
                                style = ComandaAiTypography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onRefresh) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
                
                // Orders sections
                if (state.pendingOrders.isEmpty() && state.completedOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(ComandaAiSpacing.Large.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum pedido na fila",
                            style = ComandaAiTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(ComandaAiSpacing.Medium.value),
                        verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                    ) {
                        // Pending orders section
                        if (state.pendingOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Pedidos Pendentes",
                                    style = ComandaAiTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        vertical = ComandaAiSpacing.Small.value
                                    )
                                )
                            }
                            
                            items(state.pendingOrders) { order ->
                                OrderCard(
                                    order = order,
                                    isSelected = state.selectedOrderId == order.id,
                                    onCardClick = { onSelectOrder(order.id) },
                                    onMarkComplete = { onMarkOrderComplete(order.id ?: 0) },
                                    onMarkItemComplete = { itemId -> 
                                        onMarkItemComplete(order.id ?: 0, itemId)
                                    }
                                )
                            }
                        }
                        
                        // Completed orders section
                        if (state.completedOrders.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Pedidos Concluídos",
                                    style = ComandaAiTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        top = ComandaAiSpacing.Large.value,
                                        bottom = ComandaAiSpacing.Small.value
                                    )
                                )
                            }
                            
                            items(state.completedOrders) { order ->
                                OrderCard(
                                    order = order,
                                    isSelected = false,
                                    onCardClick = { },
                                    onMarkComplete = { },
                                    onMarkItemComplete = { }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // User Profile Modal
        UserProfileModal(
            isVisible = showUserModal,
            userSession = userSession,
            onDismiss = onDismissUserModal,
            onLogout = onLogout
        )
    }
}
