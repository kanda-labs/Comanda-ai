package co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.components.UserAvatar
import co.kandalabs.comandaai.sdk.session.UserSession
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing.components.UserProfileModal
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.components.ComandaAiLoadingView
import co.kandalabs.comandaai.features.attendance.presentation.screens.itemsSelection.components.ErrorView
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading
import org.jetbrains.compose.resources.painterResource
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.details.TableDetailsScreen
import co.kandalabs.comandaai.features.attendance.presentation.screens.paymentHistory.PaymentHistoryScreen
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import co.kandalabs.comandaai.features.attendance.domain.models.model.Table
import co.kandalabs.comandaai.features.attendance.domain.models.model.TableStatus
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.ui.tooling.preview.Preview

public object TablesScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<TablesViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()
        
        var showUserModal by remember { mutableStateOf(false) }
        var userSession by remember { mutableStateOf<UserSession?>(null) }
        
        LaunchedEffect(Unit) {
            viewModel.retrieveTables()
            userSession = viewModel.getUserSession()
        }

        TablesScreenContent(
            state = state,
            userSession = userSession,
            showUserModal = showUserModal,
            retry = { viewModel.retrieveTables() },
            onRefresh = { viewModel.retrieveTables() },
            onClick = { table: Table ->
                navigator?.push(TableDetailsScreen(tableId = table.id ?: 0, tableNumber = table.number))
            },
            onPaymentHistoryClick = {
                navigator?.push(PaymentHistoryScreen)
            },
            onUserAvatarClick = {
                scope.launch {
                    userSession = viewModel.getUserSession()
                    showUserModal = true
                }
            },
            onDismissUserModal = { showUserModal = false },
            onLogout = {
                viewModel.logout()
                navigator?.replaceAll(AuthModule.getLoginScreen())
            }
        )
    }
}

@Composable
private fun TablesScreenContent(
    state: TablesScreenState,
    userSession: UserSession?,
    showUserModal: Boolean,
    retry: () -> Unit,
    onRefresh: () -> Unit,
    onClick: (Table) -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onUserAvatarClick: () -> Unit,
    onDismissUserModal: () -> Unit,
    onLogout: () -> Unit
) {
    MaterialTheme {
        if (state.isLoading) {
            ComandaAiLoadingView(
                loadingImage = painterResource(Res.drawable.golden_loading)
            )
        } else if (state.error != null) {
            ErrorView(
                error = state.error,
                onRetry = retry
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ComandaAiTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {

                ComandaAiTopAppBar(
                    title = state.title,
                    actions = {
                        IconButton(
                            onClick = onPaymentHistoryClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Histórico de Pagamentos",
                                tint = ComandaAiTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = onRefresh
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar",
                                tint = ComandaAiTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
                // Welcome message
                if (userSession?.userName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ComandaAiSpacing.Large.value,
                                top = ComandaAiSpacing.Medium.value,
                                end = ComandaAiSpacing.Large.value,
                                bottom = ComandaAiSpacing.Small.value
                            ),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            userName = userSession?.userName,
                            onClick = onUserAvatarClick
                        )

                        Text(
                            text = "Olá, ${userSession.name}!",
                            style = ComandaAiTypography.titleMedium,
                            color = ComandaAiTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                TablesGrid(
                    tablesPresentations = state.tablesPresentation,
                    onClick = onClick
                )
            }
        }
        
        // User Profile Modal
        UserProfileModal(
            isVisible = showUserModal,
            userSession = userSession,
            onDismiss = onDismissUserModal,
            onLogout = onLogout
        )
    }
}

@Composable
private fun TablesGrid(
    tablesPresentations: List<TablePresentation>,
    onClick: (Table) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(ComandaAiSpacing.Large.value)

    ) {
        items(tablesPresentations) { tablePresentation ->
            TableItem(tablePresentation = tablePresentation, onClick = onClick)
        }
    }
}

@Composable
private fun TableItem(
    tablePresentation: TablePresentation,
    onClick: (Table) -> Unit
) {

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(
            containerColor = tablePresentation.backGroundColor.value
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
                .clickable(onClick = { onClick(tablePresentation.table) })
        ) {
            Text(
                text = tablePresentation.number,
                color = tablePresentation.textColor.value,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = ComandaAiTypography.displayLarge
            )
        }
    }
}

@Preview
@Composable
private fun TablesScreenPreview() {
    MaterialTheme {
        TablesScreenContent(
            state = TablesScreenState(
                tables = persistentListOf(
                    Table(
                        number = 1,
                        status = TableStatus.OCCUPIED
                    ),
                    Table(
                        number = 2,
                        status = TableStatus.FREE
                    ),
                    Table(
                        number = 3,
                        status = TableStatus.ON_PAYMENT
                    )
                ),
                isLoading = false,
                error = null
            ),
            userSession = null,
            showUserModal = false,
            retry = {},
            onRefresh = {},
            onClick = {},
            onPaymentHistoryClick = {},
            onUserAvatarClick = {},
            onDismissUserModal = {},
            onLogout = {}
        )
    }
}

