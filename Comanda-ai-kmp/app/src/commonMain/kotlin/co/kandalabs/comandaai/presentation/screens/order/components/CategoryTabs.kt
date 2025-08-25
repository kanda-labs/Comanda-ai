package co.kandalabs.comandaai.presentation.screens.order.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.kandalabs.comandaai.domain.ItemCategory

@Composable
fun CategoryTabs(
    categories: List<ItemCategory>,
    selectedCategory: ItemCategory,
    onCategorySelected: (ItemCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            CategoryTab(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTab(
    category: ItemCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = when (category) {
        ItemCategory.SKEWER -> "Espetinhos"
        ItemCategory.DRINK -> "Bebidas"
        ItemCategory.SNACK -> "Petiscos"
        ItemCategory.PROMOTIONAL -> "Promoções"
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { 
            Text(
                text = displayName,
                style = MaterialTheme.typography.labelMedium
            ) 
        },
        modifier = modifier
    )
}