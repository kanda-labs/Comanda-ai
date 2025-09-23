package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.migration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.components.ComandaAiBottomSheetModal
import co.kandalabs.comandaai.components.ComandaAiModalPresentationMode
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TableDetailsScreen
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource

public data class TableMigrationSelection(
    val originTable: Table
) : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<TableMigrationViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.currentOrThrow
        
        var showConfirmationModal by remember { mutableStateOf(false) }
        var selectedTable by remember { mutableStateOf<Table?>(null) }
        var showErrorDialog by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            viewModel.loadTables()
        }

        // Observe error state
        LaunchedEffect(state.error) {
            state.error?.let { error ->
                errorMessage = error
                showErrorDialog = true
            }
        }

        TableMigrationScreenContent(
            state = state,
            originTable = originTable,
            showConfirmationModal = showConfirmationModal,
            selectedTable = selectedTable,
            showErrorDialog = showErrorDialog,
            errorMessage = errorMessage,
            onTableClick = { table ->
                selectedTable = table
                showConfirmationModal = true
            },
            onBackClick = {
                navigator.pop()
            },
            onConfirmMigration = {
                selectedTable?.let { destinationTable ->
                    viewModel.migrateTable(
                        originTable = originTable,
                        destinationTable = destinationTable,
                        onSuccess = { (_, updatedDestination) ->
                            // Navigate back to table details with destination table
                            navigator.pop()
                            // Replace current screen with destination table details
                            navigator.replace(
                                TableDetailsScreen(
                                    tableId = updatedDestination.id ?: 0,
                                    tableNumber = updatedDestination.number
                                )
                            )
                        }
                    )
                }
            },
            onDismissModal = {
                showConfirmationModal = false
                selectedTable = null
            },
            onDismissError = {
                showErrorDialog = false
                errorMessage = ""
            }
        )
    }
}

@Composable
private fun TableMigrationScreenContent(
    state: TableMigrationState,
    originTable: Table,
    showConfirmationModal: Boolean,
    selectedTable: Table?,
    showErrorDialog: Boolean,
    errorMessage: String,
    onTableClick: (Table) -> Unit,
    onBackClick: () -> Unit,
    onConfirmMigration: () -> Unit,
    onDismissModal: () -> Unit,
    onDismissError: () -> Unit
) {
    MaterialTheme {
        if (state.isLoading) {
            ComandaAiLoadingView(
                loadingImage = painterResource(Res.drawable.golden_loading)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                ComandaAiTopAppBar(
                    title = "Migrar mesa",
                    onBackOrClose = onBackClick,
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                )

                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                Text(
                    text = "Selecione o novo número de mesa",
                    style = ComandaAiTypography.titleMedium,
                    color = ComandaAiColors.Gray700.value,
                    modifier = Modifier
                        .padding(horizontal = ComandaAiSpacing.Medium.value)
                )

                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                // Tables Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = ComandaAiSpacing.Large.value)
                ) {
                    items(state.freeTables) { table ->
                        FreeTableItem(
                            table = table,
                            onClick = { onTableClick(table) }
                        )
                    }
                }

                // Back Button
                ComandaAiButton(
                    text = "Voltar",
                    variant = ComandaAiButtonVariant.Secondary,
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ComandaAiSpacing.Large.value)
                )
            }
        }
        
        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = onDismissError,
                title = {
                    Text(
                        text = "Erro na migração",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissError) {
                        Text("OK")
                    }
                }
            )
        }

        // Confirmation Modal
        if (showConfirmationModal && selectedTable != null) {
            TableMigrationConfirmationModal(
                originTable = originTable,
                destinationTable = selectedTable,
                onConfirm = onConfirmMigration,
                onDismiss = onDismissModal
            )
        }
    }
}

@Composable
private fun FreeTableItem(
    table: Table,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiColors.Green100.value
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            Text(
                text = table.number.toString(),
                color = ComandaAiColors.Green800.value,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = ComandaAiTypography.displayLarge
            )
        }
    }
}

@Composable
private fun TableMigrationConfirmationModal(
    originTable: Table,
    destinationTable: Table,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ComandaAiBottomSheetModal(
        isVisible = true,
        title = "Confirmar troca de Mesa",
        onDismiss = onDismiss,
        presentationMode = ComandaAiModalPresentationMode.Dynamic,
        actions = {
            ComandaAiButton(
                text = "Voltar",
                variant = ComandaAiButtonVariant.Secondary,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            
            ComandaAiButton(
                text = "Confirmar",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Você está prestes a migrar a conta da mesa ${originTable.number} para a mesa ${destinationTable.number}.",
                style = ComandaAiTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Medium.value)
            )
            
            Text(
                text = "Esta ação não pode ser desfeita.",
                style = ComandaAiTypography.bodyMedium,
                color = ComandaAiColors.Gray600.value
            )
        }
    }
}