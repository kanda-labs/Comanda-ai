package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import org.jetbrains.compose.resources.painterResource
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import comandaai.features.attendance.generated.resources.Res
import comandaai.features.attendance.generated.resources.golden_loading

data class AdminOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: AdminAction
)

public object AdminScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<AdminViewModel>()
        val state = viewModel.state.collectAsState().value
        val navigator = LocalNavigator.current
        val scope = rememberCoroutineScope()
        
        var showUserModal by remember { mutableStateOf(false) }
        var userSession by remember { mutableStateOf<UserSession?>(null) }
        
        LaunchedEffect(Unit) {
            userSession = viewModel.getUserSession()
        }

        AdminScreenContent(
            state = state,
            userSession = userSession,
            showUserModal = showUserModal,
            onAction = { action ->
                when (action) {
                    AdminAction.NavigateBack -> navigator?.pop()
                    AdminAction.NavigateToCategoriesManagement -> {
                        navigator?.push(CategoriesManagementScreen)
                    }
                    AdminAction.NavigateToItemsManagement -> {
                        navigator?.push(ItemsManagementScreen)
                    }
                    AdminAction.NavigateToUsersManagement -> {
                        navigator?.push(UsersManagementScreen)
                    }
                    else -> viewModel.handleAction(action)
                }
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
private fun AdminScreenContent(
    state: AdminScreenState,
    userSession: UserSession?,
    showUserModal: Boolean,
    onAction: (AdminAction) -> Unit,
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
            SimpleErrorView(
                errorMessage = state.error,
                onRetry = { onAction(AdminAction.Retry) }
            )
        } else if (!state.hasAdminAccess) {
            UnauthorizedView(onNavigateBack = { onAction(AdminAction.NavigateBack) })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                ComandaAiTopAppBar(
                    title = state.title,
                    onBackOrClose = { onAction(AdminAction.NavigateBack) },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    actions = {
                        UserAvatar(
                            userName = userSession?.userName,
                            onClick = onUserAvatarClick
                        )
                    }
                )

                AdminOptionsGrid(onAction = onAction)
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
private fun AdminOptionsGrid(
    onAction: (AdminAction) -> Unit
) {
    val adminOptions = listOf(
        AdminOption(
            title = "Items",
            description = "Gerenciar items do cardápio",
            icon = Icons.Default.ShoppingCart,
            action = AdminAction.NavigateToItemsManagement
        ),
        AdminOption(
            title = "Usuários",
            description = "Gerenciar usuários do sistema",
            icon = Icons.Default.Person,
            action = AdminAction.NavigateToUsersManagement
        )
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value),
        verticalArrangement = Arrangement.spacedBy(ComandaAiSpacing.Medium.value),
        modifier = Modifier
            .fillMaxWidth()
            .padding(ComandaAiSpacing.Large.value)
    ) {
        items(adminOptions) { option ->
            AdminOptionCard(
                option = option,
                onClick = { onAction(option.action) }
            )
        }
    }
}

@Composable
private fun AdminOptionCard(
    option: AdminOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(ComandaAiSpacing.Medium.value))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ComandaAiSpacing.Medium.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = ComandaAiSpacing.Small.value)
            )
            
            Text(
                text = option.title,
                style = ComandaAiTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = option.description,
                style = ComandaAiTypography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = ComandaAiSpacing.xXSmall.value)
            )
        }
    }
}

@Composable
private fun UnauthorizedView(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(ComandaAiSpacing.Large.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acesso Negado",
            style = ComandaAiTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Apenas administradores e gerentes podem acessar esta área.",
            style = ComandaAiTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = ComandaAiSpacing.Medium.value,
                bottom = ComandaAiSpacing.xXLarge.value
            )
        )
        
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(ComandaAiSpacing.Medium.value)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SimpleErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(ComandaAiSpacing.Large.value),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Erro",
            style = ComandaAiTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = errorMessage,
            style = ComandaAiTypography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                top = ComandaAiSpacing.Medium.value,
                bottom = ComandaAiSpacing.xXLarge.value
            )
        )
        
        IconButton(
            onClick = onRetry,
            modifier = Modifier.background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(ComandaAiSpacing.Medium.value)
            )
        ) {
            Text(
                text = "Tentar novamente",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

