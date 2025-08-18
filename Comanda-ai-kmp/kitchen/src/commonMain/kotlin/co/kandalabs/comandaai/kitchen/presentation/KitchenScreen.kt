package co.kandalabs.comandaai.kitchen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import co.kandalabs.comandaai.kitchen.domain.model.ItemStatus
import co.kandalabs.comandaai.kitchen.presentation.components.OrderCard
import org.kodein.di.instance

class KitchenScreen : Screen {
    
    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<KitchenViewModel>()
        val state by viewModel.state.collectAsState()
        
        KitchenScreenContent(
            state = state,
            onRefresh = viewModel::refreshOrders,
            onItemStatusChange = viewModel::updateItemStatus,
            onMarkAsDelivered = viewModel::markOrderAsDelivered,
            onMarkItemAsDelivered = viewModel::markItemAsDelivered,
            onErrorDismiss = viewModel::clearError
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KitchenScreenContent(
    state: KitchenScreenState,
    onRefresh: () -> Unit,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onMarkAsDelivered: (Int) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit,
    onErrorDismiss: () -> Unit
) {
    // Preservar estado do scroll
    val listState = rememberLazyListState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cozinha", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.orders.size} pedidos ativos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (state.isConnected) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Conectado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(20.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Desconectado",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Atualizar pedidos"
                        )
                    }
                }
            )
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
                        key = { order -> order.id } // Chave estável para preservar estado
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
    }
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