package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.components.CommandaBadge
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiListItem
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.core.utils.CurrencyFormatter
import co.kandalabs.comandaai.domain.Item
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

public object ItemsManagementScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = rememberScreenModel<ItemsManagementViewModel>()

        val uiState by viewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.loadItems()
        }

        ComandaAiTheme {
            ItemsManagementScreenContent(
                uiState = uiState,
                onNavigateBack = { navigator?.pop() },
                onItemClick = { item ->
                    item.id?.let { id ->
                        navigator?.push(ItemFormScreen(id))
                    }
                },
                onAddItemClick = {
                    navigator?.push(ItemFormScreen(null))
                },
                onCategoryFilterClick = {
                    viewModel.showCategoryFilter()
                },
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                    viewModel.hideCategoryFilter()
                },
                onDismissCategoryModal = {
                    viewModel.hideCategoryFilter()
                }
            )
        }
    }
}

@Composable
private fun ItemsManagementScreenContent(
    uiState: ItemsManagementScreenState,
    onNavigateBack: () -> Unit,
    onItemClick: (Item) -> Unit,
    onAddItemClick: () -> Unit,
    onCategoryFilterClick: () -> Unit,
    onCategorySelected: (ItemCategory?) -> Unit,
    onDismissCategoryModal: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            ComandaAiTopAppBar(
                title = "Catálogo de itens",
                onBackOrClose = onNavigateBack,
                icon = Icons.Default.ArrowBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error,
                            style = ComandaAiTypography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ComandaAiSpacing.Medium.value),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CommandaBadge(
                                text = uiState.selectedCategory?.let { getCategoryDisplayName(it) } ?: "Todas",
                                containerColor = if (uiState.selectedCategory != null)
                                    getCategoryColor(uiState.selectedCategory!!)
                                else
                                    ComandaAiColors.Gray300.value,
                                contentColor = ComandaAiColors.Surface.value,
                                modifier = Modifier.clickable { onCategoryFilterClick() }
                            )
                        }

                        if (uiState.filteredItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum item encontrado",
                                    style = ComandaAiTypography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(
                                    horizontal = ComandaAiSpacing.Medium.value,
                                    vertical = ComandaAiSpacing.Small.value
                                ),
                                verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                            ) {
                                items(
                                    items = uiState.filteredItems,
                                    key = { it.id ?: it.name }
                                ) { item ->
                                    ItemCard(
                                        item = item,
                                        onClick = { onItemClick(item) }
                                    )
                                }
                            }
                        }

                        ComandaAiButton(
                            text = "Criar novo item",
                            onClick = onAddItemClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ComandaAiSpacing.Medium.value)
                        )
                    }
                }
            }
        }

        ComandaAiBottomSheetModal(
            isVisible = uiState.showCategoryModal,
            title = "Categorias",
            onDismiss = onDismissCategoryModal,
            presentationMode = ComandaAiModalPresentationMode.Full
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ComandaAiSpacing.Medium.value),
                verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
            ) {
                item {
                    ComandaAiListItem(
                        onClick = { onCategorySelected(null) },
                        contentSlot = {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Todas",
                                    style = ComandaAiTypography.bodyLarge,
                                    fontWeight = if (uiState.selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }

                items(uiState.categories) { category ->
                    ComandaAiListItem(
                        onClick = { onCategorySelected(category) },
                        contentSlot = {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getCategoryDisplayName(category),
                                    style = ComandaAiTypography.bodyLarge,
                                    fontWeight = if (uiState.selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCard(
    item: Item,
    onClick: () -> Unit
) {
    ComandaAiListItem(
        onClick = onClick,
        contentSlot = {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = ComandaAiTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                item.description?.let { desc ->
                    Text(
                        text = desc,
                        style = ComandaAiTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = CurrencyFormatter.formatCents(item.value),
                    style = ComandaAiTypography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ComandaAiColors.Primary.value
                )
            }
        },
        trailingSlot = {
            CommandaBadge(
                text = getCategoryDisplayName(item.category),
                containerColor = getCategoryColor(item.category),
                contentColor = ComandaAiColors.Surface.value
            )
        }
    )
}

private fun getCategoryDisplayName(category: ItemCategory): String {
    return when (category) {
        ItemCategory.SKEWER -> "Espetinho"
        ItemCategory.DRINK -> "Bebida"
        ItemCategory.SNACK -> "Petisco"
        ItemCategory.PROMOTIONAL -> "Promocional"
    }
}

private fun getCategoryColor(category: ItemCategory): androidx.compose.ui.graphics.Color {
    return when (category) {
        ItemCategory.SKEWER -> ComandaAiColors.Yellow600.value
        ItemCategory.DRINK -> ComandaAiColors.Blue500.value
        ItemCategory.SNACK -> ComandaAiColors.Green500.value
        ItemCategory.PROMOTIONAL -> ComandaAiColors.Primary.value
    }
}