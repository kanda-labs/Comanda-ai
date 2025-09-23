package co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.rotate
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
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.features.attendance.domain.models.enum.PaymentMethod
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components.ErrorView
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components.PaymentHistoryCard
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components.PaymentMethodDropdown
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components.TimePicker
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.components.TimePickerModal
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.tokens.ComandaAiColor
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource

object PaymentHistoryScreen : Screen {

    @Composable
    override fun Content() {
        ComandaAiTheme {

            val viewModel = rememberScreenModel<PaymentHistoryViewModel>()
            val state = viewModel.state.collectAsState().value
            val navigator = LocalNavigator.currentOrThrow

            LaunchedEffect(Unit) {
                viewModel.loadPaymentHistory()
            }

            PaymentHistoryScreenContent(
                state = state,
                onBackClick = { navigator.pop() },
                onConsiderPreviousDayChanged = viewModel::setTempConsiderPreviousDay,
                onStartTimeChanged = viewModel::setTempStartTime,
                onEndTimeChanged = viewModel::setTempEndTime,
                onPaymentMethodChanged = viewModel::setTempPaymentMethod,
                onFiltersExpandedChanged = viewModel::setFiltersExpanded,
                onShowStartTimePicker = { viewModel.setShowStartTimePicker(true) },
                onShowEndTimePicker = { viewModel.setShowEndTimePicker(true) },
                onHideStartTimePicker = { viewModel.setShowStartTimePicker(false) },
                onHideEndTimePicker = { viewModel.setShowEndTimePicker(false) },
                onApplyFilters = viewModel::applyFilters,
                onClearFilters = viewModel::clearFilters,
                onRetry = viewModel::retry
            )
        }
    }
}

@Composable
private fun PaymentHistoryScreenContent(
    state: PaymentHistoryScreenState,
    onBackClick: () -> Unit,
    onConsiderPreviousDayChanged: (Boolean) -> Unit,
    onStartTimeChanged: (Int, Int) -> Unit,
    onEndTimeChanged: (Int, Int) -> Unit,
    onPaymentMethodChanged: (PaymentMethod?) -> Unit,
    onFiltersExpandedChanged: (Boolean) -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    onHideStartTimePicker: () -> Unit,
    onHideEndTimePicker: () -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit
) {
    ComandaAiTheme {
        Column {
            if (state.isLoading) {
                ComandaAiLoadingView(
                    loadingImage = painterResource(Res.drawable.golden_loading)
                )
            } else if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Erro ao carregar histórico",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.error.message ?: "Erro desconhecido",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        ComandaAiButton(
                            text = "Tentar Novamente",
                            onClick = onRetry
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComandaAiColors.Background.value)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Histórico de Pagamentos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(ComandaAiSpacing.Large.value)
                                .padding(top = ComandaAiSpacing.xXLarge.value)
                                .padding(top = ComandaAiSpacing.Small.value),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            // Summary Section
                            item {
                                SummarySection(state = state)
                            }

                            // Payments List
                            if (state.payments.isEmpty()) {
                                item {
                                    EmptyStateSection()
                                }
                            } else {
                                items(state.payments) { payment ->
                                    PaymentHistoryCard(payment = payment)
                                }
                            }
                        }

                        FiltersSection(
                            state = state,
                            onConsiderPreviousDayChanged = onConsiderPreviousDayChanged,
                            onPaymentMethodChanged = onPaymentMethodChanged,
                            onFiltersExpandedChanged = onFiltersExpandedChanged,
                            onShowStartTimePicker = onShowStartTimePicker,
                            onShowEndTimePicker = onShowEndTimePicker,
                            onApplyFilters = onApplyFilters,
                            onClearFilters = onClearFilters
                        )

                        TimePickerModal(
                            isVisible = state.showStartTimePicker,
                            title = "Selecionar Hora Inicial",
                            currentHour = state.tempStartHour,
                            currentMinute = state.tempStartMinute,
                            onTimeChanged = onStartTimeChanged,
                            onDismiss = onHideStartTimePicker
                        )

                        TimePickerModal(
                            isVisible = state.showEndTimePicker,
                            title = "Selecionar Hora Final",
                            currentHour = state.tempEndHour,
                            currentMinute = state.tempEndMinute,
                            onTimeChanged = onEndTimeChanged,
                            onDismiss = onHideEndTimePicker
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersSection(
    state: PaymentHistoryScreenState,
    onConsiderPreviousDayChanged: (Boolean) -> Unit,
    onPaymentMethodChanged: (PaymentMethod?) -> Unit,
    onFiltersExpandedChanged: (Boolean) -> Unit,
    onShowStartTimePicker: () -> Unit,
    onShowEndTimePicker: () -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit
) {
    val isExpanded = state.isFiltersExpanded
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = ComandaAiSpacing.Large.value),
        colors = CardDefaults.cardColors(
            containerColor = ComandaAiColors.Gray200.value
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header do accordeon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFiltersExpandedChanged(!isExpanded) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Recolher" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }

            // Conteúdo do accordeon
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Switch for considering previous day
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Considerar o dia anterior",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = state.tempConsiderPreviousDay,
                            onCheckedChange = onConsiderPreviousDayChanged
                        )
                    }

                    // Time filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimePicker(
                            label = "Hora Inicial",
                            hour = state.tempStartHour,
                            minute = state.tempStartMinute,
                            onShowTimePicker = onShowStartTimePicker,
                            modifier = Modifier.weight(1f)
                        )

                        TimePicker(
                            label = "Hora Final",
                            hour = state.tempEndHour,
                            minute = state.tempEndMinute,
                            onShowTimePicker = onShowEndTimePicker,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    PaymentMethodDropdown(
                        selectedPaymentMethod = state.tempPaymentMethod,
                        onPaymentMethodSelected = onPaymentMethodChanged
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ComandaAiButton(
                            text = "Limpar Filtros",
                            onClick = onClearFilters,
                            modifier = Modifier.weight(1f),
                            variant = ComandaAiButtonVariant.Secondary
                        )

                        ComandaAiButton(
                            text = "Aplicar Filtros",
                            onClick = onApplyFilters,
                            modifier = Modifier.weight(1f),
                            isEnabled = state.hasUnappliedChanges
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(state: PaymentHistoryScreenState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total de Pagamentos:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = state.totalAmountFormatted,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            Text(
                text = "${state.payments.size} pagamento(s) encontrado(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyStateSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nenhum pagamento encontrado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Ajuste os filtros para encontrar seus pagamentos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}