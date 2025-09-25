package co.kandalabs.comandaai.features.attendance.presentation.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.features.attendance.domain.model.ItemWithCount
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TableDetailsScreen
import kotlinx.coroutines.delay

data class OrderConfirmationScreen(
    private val tableNumber: String,
    private val tableId: Int,
    private val billId: Int,
    private val screenModel: OrderScreenModel
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val selectedItems by screenModel.selectedItemsWithCount.collectAsState()
        val totalItems by screenModel.totalItems.collectAsState()
        val orderSubmissionState by screenModel.orderSubmissionState.collectAsState()
        val orderSubmissionError by screenModel.orderSubmissionError.collectAsState()
        val itemObservations by screenModel.itemObservations.collectAsState()

        LaunchedEffect(orderSubmissionState) {
            if (orderSubmissionState == OrderSubmissionState.SUCCESS) {
                delay(1000)
                screenModel.resetOrderSubmissionState()
                navigator.popUntil { screen -> screen is TableDetailsScreen }
            }
        }

        OrderConfirmationContent(
            tableNumber = tableNumber,
            selectedItems = selectedItems,
            totalItems = totalItems,
            itemObservations = itemObservations,
            orderSubmissionState = orderSubmissionState,
            orderSubmissionError = orderSubmissionError,
            onBackClick = {
                screenModel.resetOrderSubmissionState()
                if (orderSubmissionState == OrderSubmissionState.ERROR) {
                    navigator.popUntil { screen -> screen is TableDetailsScreen }
                } else {
                    navigator.pop()
                }
            },
            onConfirm = {
                if (orderSubmissionState != OrderSubmissionState.LOADING) {
                    screenModel.submitOrder(tableId, billId)
                }
            },
            onRetry = {
                if (orderSubmissionState != OrderSubmissionState.LOADING) {
                    screenModel.submitOrder(tableId, billId)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderConfirmationContent(
    tableNumber: String,
    selectedItems: List<ItemWithCount>,
    totalItems: Int,
    itemObservations: Map<Int, String>,
    orderSubmissionState: OrderSubmissionState,
    orderSubmissionError: String?,
    onBackClick: () -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = ComandaAiTheme.colorScheme.background,
        contentColor = ComandaAiTheme.colorScheme.onBackground,
        topBar = {
            ComandaAiTopAppBar(
                title = "Confirmar Pedido",
                onBackOrClose = onBackClick,
                icon = Icons.AutoMirrored.Filled.ArrowBack
            )
        }
    ) { paddingValues ->
        when (orderSubmissionState) {
            OrderSubmissionState.IDLE -> {
                IdleContent(
                    selectedItems = selectedItems,
                    totalItems = totalItems,
                    tableNumber = tableNumber,
                    itemObservations = itemObservations,
                    onConfirm = onConfirm,
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            OrderSubmissionState.LOADING -> {
                LoadingContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            OrderSubmissionState.SUCCESS -> {
                SuccessContent(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            OrderSubmissionState.ERROR -> {
                ErrorContent(
                    errorMessage = orderSubmissionError,
                    onRetry = onRetry,
                    onBackClick = onBackClick,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun IdleContent(
    selectedItems: List<ItemWithCount>,
    tableNumber: String,
    totalItems: Int,
    itemObservations: Map<Int, String>,
    onConfirm: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (selectedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum item selecionado",
                    style = ComandaAiTheme.typography.bodyMedium,
                    color = ComandaAiTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Mesa $tableNumber",
                    style = ComandaAiTheme.typography.titleMedium,
                    color = ComandaAiTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                Text(
                    text = "Itens selecionados:",
                    style = ComandaAiTheme.typography.titleMedium,
                    color = ComandaAiTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedItems) { itemWithCount ->
                        OrderConfirmationItem(
                            itemWithCount = itemWithCount,
                            observation = itemWithCount.item.id?.let { itemObservations[it] }
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp,
            color = ComandaAiTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (selectedItems.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ComandaAiTheme.colorScheme.primaryContainer
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
                                text = "Total:",
                                style = ComandaAiTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ComandaAiTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$totalItems ${if (totalItems == 1) "item" else "itens"}",
                                style = ComandaAiTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ComandaAiTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ComandaAiTheme.colorScheme.primary,
                            disabledContentColor = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Text("Voltar")
                    }

                    ComandaAiButton(
                        text = "Confirmar",
                        onClick = onConfirm,
                        isEnabled = selectedItems.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 6.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Processando pedido...",
            style = ComandaAiTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = ComandaAiTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Por favor, aguarde",
            style = ComandaAiTheme.typography.bodyMedium,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = ComandaAiTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pedido realizado com sucesso!",
            style = ComandaAiTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ComandaAiTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Seu pedido foi enviado para a cozinha",
            style = ComandaAiTheme.typography.bodyLarge,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Voltando automaticamente...",
            style = ComandaAiTheme.typography.bodySmall,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(
    errorMessage: String?,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = ComandaAiTheme.colorScheme.error,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Erro ao processar pedido",
            style = ComandaAiTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ComandaAiTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = errorMessage ?: "Erro desconhecido",
            style = ComandaAiTheme.typography.bodyLarge,
            color = ComandaAiTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ComandaAiTheme.colorScheme.primary,
                    disabledContentColor = ComandaAiTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text("Voltar")
            }

            ComandaAiButton(
                text = "Tentar Novamente",
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OrderConfirmationItem(
    itemWithCount: ItemWithCount,
    observation: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiTheme.colorScheme.surfaceVariant
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
                    text = itemWithCount.item.name,
                    style = ComandaAiTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ComandaAiTheme.colorScheme.onSurfaceVariant
                )
                if (itemWithCount.item.description?.isNotEmpty() == true)
                    itemWithCount.item.description?.let { description ->
                        Text(
                            text = description,
                            style = ComandaAiTheme.typography.bodySmall,
                            color = ComandaAiTheme.colorScheme.onSurfaceVariant
                        )
                    }
                if (!observation.isNullOrBlank()) {
                    Text(
                        text = "Obs: $observation",
                        style = ComandaAiTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = ComandaAiTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = "${itemWithCount.count}x",
                style = ComandaAiTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ComandaAiTheme.colorScheme.primary
            )
        }
    }
}