package co.kandalabs.comandaai.features.attendance.presentation.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiOrderItemCard
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.components.OrderItemData
import co.kandalabs.comandaai.features.attendance.presentation.screens.order.components.CategoryTabs
import co.kandalabs.comandaai.features.attendance.presentation.screens.order.components.ObservationModal
import co.kandalabs.comandaai.features.attendance.presentation.screens.order.components.OrderConfirmationModal
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.features.attendance.domain.model.ItemWithCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreenContent(
    tableNumber: String,
    screenModel: OrderScreenModel,
    onBackClick: () -> Unit,
    onSubmitOrder: () -> Unit,
    onOrderSuccess: () -> Unit,
    onShowFeedback: (isSuccess: Boolean, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = screenModel.categories
    val selectedCategory by screenModel.selectedCategory.collectAsState()
    val itemsWithCount: List<ItemWithCount> by screenModel.itemsWithCount.collectAsState()
    val canSubmit by screenModel.canSubmit.collectAsState()
    val totalItems by screenModel.totalItems.collectAsState()
    val isLoading by screenModel.isLoading.collectAsState()
    val error by screenModel.error.collectAsState()
    val orderSubmitted by screenModel.orderSubmitted.collectAsState()
    val showConfirmationModal by screenModel.showConfirmationModal.collectAsState()
    val selectedItemsWithCount by screenModel.selectedItemsWithCount.collectAsState()
    val isSubmitting by screenModel.isSubmitting.collectAsState()
    val showObservationModal by screenModel.showObservationModal.collectAsState()
    val selectedItemForObservation by screenModel.selectedItemForObservation.collectAsState()
    val currentObservationForSelectedItem by screenModel.currentObservationForSelectedItem.collectAsState()
    val selectedItemHasQuantity by screenModel.selectedItemHasQuantity.collectAsState()
    
    // Handle order submission success
    LaunchedEffect(orderSubmitted) {
        if (orderSubmitted) {
            screenModel.resetOrderSubmitted()
            onOrderSuccess()
        }
    }
    
    // Handle order submission error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            onShowFeedback(false, errorMessage)
            screenModel.clearError()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            topBar = {
                ComandaAiTopAppBar(
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
                        ComandaAiButton(
                            text = "Fazer pedido ($totalItems ${if (totalItems == 1) "item" else "itens"})",
                            onClick = { screenModel.showConfirmationModal() },
                            isEnabled = !isLoading && !isSubmitting,
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
                                ComandaAiOrderItemCard(
                                    item = OrderItemData(
                                        name = itemWithCount.item.name,
                                        value = itemWithCount.item.value,
                                        description = itemWithCount.item.description,
                                        count = itemWithCount.count
                                    ),
                                    onIncrement = { 
                                        itemWithCount.item.id?.let { id ->
                                            screenModel.incrementItem(id)
                                        }
                                    },
                                    onDecrement = { 
                                        itemWithCount.item.id?.let { id ->
                                            screenModel.decrementItem(id)
                                        }
                                    },
                                    onLongClick = {
                                        screenModel.showObservationModal(itemWithCount.item)
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
            isLoading = isSubmitting,
            onConfirm = onSubmitOrder,
            onDismiss = { screenModel.hideConfirmationModal() }
        )
        
        // Observation Modal - overlays the entire screen
        ObservationModal(
            isVisible = showObservationModal,
            item = selectedItemForObservation,
            currentObservation = currentObservationForSelectedItem,
            hasItemsSelected = selectedItemHasQuantity,
            onDismiss = { screenModel.hideObservationModal() },
            onAddWithObservation = { observation ->
                screenModel.addItemWithObservation(observation)
            }
        )
    }
}