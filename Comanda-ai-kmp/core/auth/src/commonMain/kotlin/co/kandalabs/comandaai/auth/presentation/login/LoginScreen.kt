package co.kandalabs.comandaai.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.tokens.ComandaAiColors
import co.kandalabs.comandaai.tokens.ComandaAiSpacing
import org.jetbrains.compose.ui.tooling.preview.Preview

object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel<LoginViewModel>()
        val state by viewModel.state.collectAsState()

        LoginScreenContent(
            state = state,
            onUsernameChanged = viewModel::onUsernameChanged,
            onPasswordChanged = viewModel::onPasswordChanged,
            onLogin = viewModel::onLogin,
            onClearError = viewModel::clearError,
            onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
            onValidateUsername = viewModel::validateUsernameField,
            onValidatePassword = viewModel::validatePasswordField
        )
    }
}

@Composable
private fun LoginScreenContent(
    state: LoginScreenState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onClearError: () -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onValidateUsername: () -> Unit,
    onValidatePassword: () -> Unit
) {
    val focusManager = LocalFocusManager.current


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComandaAiTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(ComandaAiSpacing.Medium.value)
    ) {
        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dynamic spacing that reduces when keyboard is up
            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

        // Logo/Title
        Text(
            text = "ComandaAi!",
            style = ComandaAiTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = ComandaAiTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Subtitle
        Text(
            text = "Faça login para continuar",
            style = ComandaAiTheme.typography.bodyLarge,
            color = ComandaAiTheme.colorScheme.gray700,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Username Field
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = { Text("Usuário") },
            placeholder = { Text("Digite seu usuário") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && state.username.isNotEmpty()) {
                        onValidateUsername()
                    }
                },
            shape = RoundedCornerShape(ComandaAiSpacing.Small.value),
            isError = state.usernameError != null,
            supportingText = {
                state.usernameError?.let { error ->
                    Text(
                        text = error,
                        color = ComandaAiTheme.colorScheme.error,
                        style = ComandaAiTheme.typography.bodySmall
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ComandaAiTheme.colorScheme.primary,
                unfocusedBorderColor = ComandaAiTheme.colorScheme.gray400,
                errorBorderColor = ComandaAiTheme.colorScheme.error,
                focusedLabelColor = ComandaAiTheme.colorScheme.primary,
                unfocusedLabelColor = ComandaAiTheme.colorScheme.gray600,
                errorLabelColor = ComandaAiTheme.colorScheme.error,
                focusedTextColor = ComandaAiTheme.colorScheme.onBackground,
                unfocusedTextColor = ComandaAiTheme.colorScheme.onBackground,
                focusedPlaceholderColor = ComandaAiTheme.colorScheme.gray500,
                unfocusedPlaceholderColor = ComandaAiTheme.colorScheme.gray500
            )
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

        // Password Field
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Senha") },
            placeholder = { Text("Digite sua senha") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused && state.password.isNotEmpty()) {
                        onValidatePassword()
                    }
                },
            shape = RoundedCornerShape(ComandaAiSpacing.Small.value),
            isError = state.passwordError != null,
            supportingText = {
                state.passwordError?.let { error ->
                    Text(
                        text = error,
                        color = ComandaAiTheme.colorScheme.error,
                        style = ComandaAiTheme.typography.bodySmall
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) 
                VisualTransformation.None 
            else 
                PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = onTogglePasswordVisibility) {
                    Text(
                        text = if (state.isPasswordVisible) "Ocultar" else "Mostrar",
                        style = ComandaAiTheme.typography.bodySmall,
                        color = ComandaAiTheme.colorScheme.primary
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (state.isContinueButtonEnabled) {
                        onLogin()
                    }
                }
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ComandaAiTheme.colorScheme.primary,
                unfocusedBorderColor = ComandaAiTheme.colorScheme.gray400,
                errorBorderColor = ComandaAiTheme.colorScheme.error,
                focusedLabelColor = ComandaAiTheme.colorScheme.primary,
                unfocusedLabelColor = ComandaAiTheme.colorScheme.gray600,
                errorLabelColor = ComandaAiTheme.colorScheme.error,
                focusedTextColor = ComandaAiTheme.colorScheme.onBackground,
                unfocusedTextColor = ComandaAiTheme.colorScheme.onBackground,
                focusedPlaceholderColor = ComandaAiTheme.colorScheme.gray500,
                unfocusedPlaceholderColor = ComandaAiTheme.colorScheme.gray500
            )
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Extra space to ensure content is scrollable above keyboard
        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))
        }

        // Bottom section with login button and error message
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Error Message
            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ComandaAiTheme.colorScheme.error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(ComandaAiSpacing.Small.value)
                ) {
                    Text(
                        text = error.message,
                        color = ComandaAiTheme.colorScheme.error,
                        style = ComandaAiTheme.typography.bodyMedium,
                        modifier = Modifier.padding(ComandaAiSpacing.Medium.value),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
            }

            // Login Button
            ComandaAiButton(
                text = if (state.isLoading) "Entrando..." else "Continuar",
                onClick = onLogin,
                isEnabled = state.isContinueButtonEnabled,
                variant = ComandaAiButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Loading overlay
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = ComandaAiTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenContent(
            state = LoginScreenState(
                username = "usuario",
                password = "",
                usernameError = null,
                passwordError = "A senha deve conter pelo menos 4 caracteres"
            ),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLogin = {},
            onClearError = {},
            onTogglePasswordVisibility = {},
            onValidateUsername = {},
            onValidatePassword = {}
        )
    }
}