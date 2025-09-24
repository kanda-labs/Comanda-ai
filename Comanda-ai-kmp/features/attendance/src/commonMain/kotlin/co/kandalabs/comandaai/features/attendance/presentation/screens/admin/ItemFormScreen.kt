package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.domain.ItemCategory
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

data class ItemFormScreen(val itemId: Int?) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = rememberScreenModel<ItemFormViewModel>()

        val uiState by viewModel.uiState.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(itemId) {
            itemId?.let {
                viewModel.loadItem(it)
            }
        }

        ComandaAiTheme {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                topBar = {
                    ComandaAiTopAppBar(
                        title = if (itemId == null) "Novo Item" else "Editar Item",
                        onBackOrClose = { navigator?.pop() },
                        icon = Icons.Default.ArrowBack
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(ComandaAiTheme.colorScheme.background)
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
                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                        .padding(ComandaAiSpacing.Medium.value)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.name,
                                        onValueChange = { viewModel.updateName(it) },
                                        label = { Text("Nome do item") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = uiState.nameError != null
                                    )
                                    uiState.nameError?.let {
                                        Text(
                                            text = it,
                                            color = ComandaAiTheme.colorScheme.error,
                                            style = ComandaAiTypography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                                    OutlinedTextField(
                                        value = uiState.description,
                                        onValueChange = { viewModel.updateDescription(it) },
                                        label = { Text("Descrição (opcional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                                    OutlinedTextField(
                                        value = uiState.priceDisplay,
                                        onValueChange = { viewModel.updatePrice(it) },
                                        label = { Text("Preço (R$)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        isError = uiState.priceError != null
                                    )
                                    uiState.priceError?.let {
                                        Text(
                                            text = it,
                                            color = ComandaAiTheme.colorScheme.error,
                                            style = ComandaAiTypography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "💡 Dica: Defina o preço como R$ 0,00 para desativar o item",
                                        color = ComandaAiTheme.colorScheme.onSurfaceVariant,
                                        style = ComandaAiTypography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )

                                    Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                                    var expanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = getCategoryDisplayName(uiState.category),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Categoria") },
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                            },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth(),
                                            isError = uiState.categoryError != null
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            ItemCategory.entries.forEach { category ->
                                                DropdownMenuItem(
                                                    text = { Text(getCategoryDisplayName(category)) },
                                                    onClick = {
                                                        viewModel.updateCategory(category)
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    uiState.categoryError?.let {
                                        Text(
                                            text = it,
                                            color = ComandaAiTheme.colorScheme.error,
                                            style = ComandaAiTypography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }

                                    uiState.error?.let { error ->
                                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = ComandaAiTheme.colorScheme.errorContainer
                                            )
                                        ) {
                                            Text(
                                                text = error,
                                                color = ComandaAiTheme.colorScheme.onErrorContainer,
                                                style = ComandaAiTypography.bodyMedium,
                                                modifier = Modifier.padding(ComandaAiSpacing.Medium.value)
                                            )
                                        }
                                    }
                                }

                                // Botões na parte inferior
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(ComandaAiSpacing.Medium.value),
                                    verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                                ) {
                                    ComandaAiButton(
                                        text = if (itemId == null) "Criar Item" else "Salvar Alterações",
                                        onClick = {
                                            viewModel.saveItem(
                                                onSuccess = {
                                                    navigator?.pop()
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        isEnabled = !uiState.isSaving,
                                        variant = ComandaAiButtonVariant.Primary
                                    )

                                    if (itemId != null) {
                                        ComandaAiButton(
                                            text = "Deletar",
                                            onClick = { showDeleteDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            variant = ComandaAiButtonVariant.Destructive
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ComandaAiBottomSheetModal(
                    isVisible = showDeleteDialog,
                    title = "Confirmar Exclusão",
                    onDismiss = { showDeleteDialog = false },
                    presentationMode = ComandaAiModalPresentationMode.Dynamic
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = ComandaAiSpacing.Medium.value),
                        verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Large.value)
                    ) {
                        Text(
                            text = "Tem certeza que deseja deletar este item? Esta ação não pode ser desfeita.",
                            style = ComandaAiTypography.bodyLarge,
                            color = ComandaAiTheme.colorScheme.onSurface
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Small.value)
                        ) {
                            ComandaAiButton(
                                text = "Deletar",
                                onClick = {
                                    viewModel.deleteItem(
                                        onSuccess = {
                                            navigator?.pop()
                                        }
                                    )
                                    showDeleteDialog = false
                                },
                                modifier = Modifier.fillMaxWidth(),
                                variant = ComandaAiButtonVariant.Destructive
                            )

                            ComandaAiButton(
                                text = "Cancelar",
                                onClick = { showDeleteDialog = false },
                                modifier = Modifier.fillMaxWidth(),
                                variant = ComandaAiButtonVariant.Secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryDisplayName(category: ItemCategory?): String {
    return when (category) {
        ItemCategory.SKEWER -> "Espetinho"
        ItemCategory.DRINK -> "Bebida"
        ItemCategory.SNACK -> "Petisco"
        ItemCategory.PROMOTIONAL -> "Promocional"
        null -> ""
    }
}