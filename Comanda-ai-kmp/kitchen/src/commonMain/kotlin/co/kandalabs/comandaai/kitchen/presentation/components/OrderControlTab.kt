package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenOrder
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreenState
import co.kandalabs.comandaai.kitchen.presentation.OrderFilter

@Composable
fun OrderControlTab(
    state: KitchenScreenState,
    listState: LazyListState,
    onItemStatusChange: (Int, Int, Int, ItemStatus) -> Unit,
    onMarkItemAsDelivered: (Int, Int) -> Unit,
    onShowDeliveryConfirmation: (KitchenOrder) -> Unit = {},
    removingOrderIds: Set<Int> = emptySet(),
    onOrderRemovalComplete: (Int) -> Unit = {}
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
                        onMarkItemAsDelivered = onMarkItemAsDelivered,
                        onShowDeliveryConfirmation = onShowDeliveryConfirmation,
                        isDeliveredView = state.currentFilter == OrderFilter.DELIVERED,
                        loadingItemIds = state.loadingItemIds,
                        isRemoving = removingOrderIds.contains(order.id),
                        onRemovalAnimationComplete = { onOrderRemovalComplete(order.id) }
                    )
                }
            }
        }
    }
}