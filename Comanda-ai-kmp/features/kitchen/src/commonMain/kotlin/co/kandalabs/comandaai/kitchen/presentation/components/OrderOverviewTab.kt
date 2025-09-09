package co.kandalabs.comandaai.kitchen.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.domain.ItemStatus
import co.kandalabs.comandaai.kitchen.domain.model.KitchenItemDetail
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreenState

@Composable
fun OrderOverviewTab(state: KitchenScreenState) {
    var selectedCategories by remember { mutableStateOf(setOf(ItemCategory.SKEWER)) }

    val itemSummary = remember(state.activeOrders, selectedCategories) {
        state.activeOrders.flatMap { order ->
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
                state.activeOrders.flatMap { it.items }
                    .first { it.itemId == itemId }
            }
            .filter { (_, count) -> count > 0 } // Remover itens com contagem zero
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category Filter - Horizontal Badges
        CategoryFilterRow(
            selectedCategories = selectedCategories,
            onCategoryToggle = { category ->
                selectedCategories = if (selectedCategories.contains(category)) {
                    selectedCategories - category
                } else {
                    selectedCategories + category
                }
            }
        )

        // Items Summary
        if (itemSummary.isEmpty()) {
            EmptyOverviewState()
        } else {
            ItemsSummaryList(itemSummary = itemSummary)
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategories: Set<ItemCategory>,
    onCategoryToggle: (ItemCategory) -> Unit
) {
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
            CategoryFilterChip(
                categoryName = categoryName,
                isSelected = selectedCategories.contains(categoryEnum),
                onClick = { onCategoryToggle(categoryEnum) }
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    categoryName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
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

@Composable
private fun EmptyOverviewState() {
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
}

@Composable
private fun ItemsSummaryList(itemSummary: Map<KitchenItemDetail, Int>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
    ) {
        items(itemSummary.toList()) { (item, totalQuantity) ->
            ItemSummaryCard(
                item = item,
                totalQuantity = totalQuantity
            )
        }
    }
}

@Composable
private fun ItemSummaryCard(
    item: KitchenItemDetail,
    totalQuantity: Int
) {
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