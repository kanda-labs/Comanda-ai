package co.touchlab.dogify.presentation.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.dogify.components.DogifyButton
import co.touchlab.dogify.components.DogifyTopAppBar
import co.touchlab.dogify.presentation.screens.order.components.CategoryTabs
import co.touchlab.dogify.presentation.screens.order.components.OrderConfirmationModal
import co.touchlab.dogify.presentation.screens.order.components.OrderItemCard
import kandalabs.commander.domain.model.ItemCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenContent(
    tableNumber: String,
    screenModel: OrderScreenModel,
    onBackClick: () -> Unit,
    onSubmitOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = screenModel.categories
    val selectedCategory by screenModel.selectedCategory.collectAsState()
    val itemsWithCount by screenModel.itemsWithCount.collectAsState()
    val canSubmit by screenModel.canSubmit.collectAsState()
    val totalItems by screenModel.totalItems.collectAsState()
    val isLoading by screenModel.isLoading.collectAsState()
    val error by screenModel.error.collectAsState()
    val orderSubmitted by screenModel.orderSubmitted.collectAsState()
    val showConfirmationModal by screenModel.showConfirmationModal.collectAsState()
    val selectedItemsWithCount by screenModel.selectedItemsWithCount.collectAsState()
    
    // Handle order submission success
    LaunchedEffect(orderSubmitted) {
        if (orderSubmitted) {
            screenModel.hideConfirmationModal()
            onBackClick()
            screenModel.resetOrderSubmitted()
        }
    }
    
    // Show error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // In a real app, you'd show a Snackbar here
            println("Error: $errorMessage")
            screenModel.clearError()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                DogifyTopAppBar(
                    title = "Mesa $tableNumber",
                    onBackOrClose = onBackClick,
                    icon = Icons.AutoMirrored.Filled.ArrowBack
                )
            },
            bottomBar = {
                if (canSubmit) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 8.dp
                    ) {
                        DogifyButton(
                            text = "Fazer pedido ($totalItems ${if (totalItems == 1) "item" else "itens"})",
                            onClick = { screenModel.showConfirmationModal() },
                            isEnabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Subtitle
            Text(
                text = "Fazer pedido",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Category tabs
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { screenModel.selectCategory(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading && itemsWithCount.isEmpty() -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    itemsWithCount.isEmpty() -> {
                        Text(
                            text = "Nenhum item encontrado nesta categoria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(itemsWithCount) { itemWithCount ->
                                OrderItemCard(
                                    itemWithCount = itemWithCount,
                                    onIncrement = { 
                                        itemWithCount.item.id?.let { id ->
                                            screenModel.incrementItem(id)
                                        }
                                    },
                                    onDecrement = { 
                                        itemWithCount.item.id?.let { id ->
                                            screenModel.decrementItem(id)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
        
        // Order Confirmation Modal - overlays the entire screen
        OrderConfirmationModal(
            isVisible = showConfirmationModal,
            selectedItems = selectedItemsWithCount,
            totalItems = totalItems,
            isLoading = isLoading,
            onConfirm = onSubmitOrder,
            onDismiss = { screenModel.hideConfirmationModal() }
        )
    }
}