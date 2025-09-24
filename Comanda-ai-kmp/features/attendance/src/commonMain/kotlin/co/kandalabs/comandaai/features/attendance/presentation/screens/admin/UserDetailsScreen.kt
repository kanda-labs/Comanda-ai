package co.kandalabs.comandaai.features.attendance.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenuItem
import co.kandalabs.comandaai.theme.ComandaAiTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.core.session.UserRole
import co.kandalabs.comandaai.features.attendance.domain.usecases.GetUserByIdUseCase
import co.kandalabs.comandaai.features.attendance.domain.usecases.UpdateUserUseCase
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import org.kodein.di.compose.localDI
import org.kodein.di.instance

data class UserDetailsScreen(val userId: Int) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = rememberScreenModel<UserDetailsViewModel>()

        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        var userName by remember { mutableStateOf("") }
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var isActive by remember { mutableStateOf(true) }
        var selectedRole by remember { mutableStateOf(UserRole.WAITER) }
        var isRoleDropdownExpanded by remember { mutableStateOf(false) }

        val roles = listOf(
            UserRole.ADMIN to "Administrador",
            UserRole.MANAGER to "Gerente",
            UserRole.WAITER to "Garçom",
            UserRole.KITCHEN to "Cozinha"
        )

        // Initialize fields when user is loaded
        LaunchedEffect(uiState.user) {
            uiState.user?.let { user ->
                userName = user.userName
                fullName = user.name
                email = user.email ?: ""
                isActive = user.active
                selectedRole = user.role
            }
        }

        // Load user on first composition
        LaunchedEffect(Unit) {
            viewModel.setUserId(userId)
            viewModel.loadUser()
        }

        // Handle success and error messages
        LaunchedEffect(uiState.successMessage) {
            uiState.successMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
                viewModel.clearMessages()
            }
        }

        LaunchedEffect(uiState.error) {
            uiState.error?.let { error ->
                snackbarHostState.showSnackbar(error)
                viewModel.clearMessages()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ComandaAiTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            ComandaAiTopAppBar(
                title = "Detalhes do Usuário",
                onBackOrClose = { navigator?.pop() },
                icon = Icons.Default.ArrowBack
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(ComandaAiSpacing.Large.value)
                        .verticalScroll(rememberScrollState())
                ) {

                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Nome de usuário") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading && !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nome completo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !uiState.isLoading && !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail (opcional)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !uiState.isLoading && !uiState.isSaving
                        )

                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                        ExposedDropdownMenuBox(
                            expanded = isRoleDropdownExpanded,
                            onExpandedChange = {
                                if (!uiState.isLoading && !uiState.isSaving) {
                                    isRoleDropdownExpanded = !isRoleDropdownExpanded
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = roles.find { it.first == selectedRole }?.second ?: "Garçom",
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Função") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isRoleDropdownExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                enabled = !uiState.isLoading && !uiState.isSaving
                            )

                            ExposedDropdownMenu(
                                expanded = isRoleDropdownExpanded,
                                onDismissRequest = { isRoleDropdownExpanded = false }
                            ) {
                                roles.forEach { (role, displayName) ->
                                    DropdownMenuItem(
                                        text = { Text(displayName) },
                                        onClick = {
                                            selectedRole = role
                                            isRoleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ComandaAiSpacing.Medium.value),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Status do usuário",
                                    style = ComandaAiTypography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isActive) "Ativo" else "Inativo",
                                    style = ComandaAiTypography.bodyMedium,
                                    color = ComandaAiTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                enabled = !uiState.isLoading && !uiState.isSaving
                            )
                        }

                }

                ComandaAiButton(
                    text = if (uiState.isSaving) "Salvando..." else "Salvar Alterações",
                    onClick = {
                        viewModel.updateUser(
                            userName = userName,
                            fullName = fullName,
                            email = email,
                            isActive = isActive,
                            role = selectedRole
                        )
                    },
                    modifier = Modifier.padding(ComandaAiSpacing.Large.value),
                    isEnabled = !uiState.isLoading && !uiState.isSaving &&
                               userName.isNotBlank() && fullName.isNotBlank()
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(ComandaAiSpacing.Medium.value)
            )
        }
    }
}
