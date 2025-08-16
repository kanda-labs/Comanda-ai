package co.kandalabs.comandaai.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import co.kandalabs.comandaai.components.ComandaAiButton
import co.kandalabs.comandaai.components.ComandaAiButtonVariant
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
            onClearError = viewModel::clearError
        )
    }
}

@Composable
private fun LoginScreenContent(
    state: LoginScreenState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLogin: () -> Unit,
    onClearError: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Clear error when state changes
    LaunchedEffect(state.username, state.password) {
        if (state.error != null) {
            onClearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComandaAiColors.Background.value)
            .padding(ComandaAiSpacing.Medium.value)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Logo/Title
        Text(
            text = "ComandaAi!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = ComandaAiColors.Primary.value,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Subtitle
        Text(
            text = "Faça login para continuar",
            style = MaterialTheme.typography.bodyLarge,
            color = ComandaAiColors.Gray700.value,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Username Field
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChanged,
            label = { Text("Usuário") },
            placeholder = { Text("Digite seu usuário") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(ComandaAiSpacing.Small.value),
            isError = state.usernameError != null,
            supportingText = {
                state.usernameError?.let { error ->
                    Text(
                        text = error,
                        color = ComandaAiColors.Error.value,
                        style = MaterialTheme.typography.bodySmall
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
                focusedBorderColor = ComandaAiColors.Primary.value,
                unfocusedBorderColor = ComandaAiColors.Gray400.value,
                errorBorderColor = ComandaAiColors.Error.value,
                focusedLabelColor = ComandaAiColors.Primary.value,
                unfocusedLabelColor = ComandaAiColors.Gray600.value,
                errorLabelColor = ComandaAiColors.Error.value
            )
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))

        // Password Field
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            label = { Text("Senha") },
            placeholder = { Text("Digite sua senha") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(ComandaAiSpacing.Small.value),
            isError = state.passwordError != null,
            supportingText = {
                state.passwordError?.let { error ->
                    Text(
                        text = error,
                        color = ComandaAiColors.Error.value,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            visualTransformation = PasswordVisualTransformation(),
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
                focusedBorderColor = ComandaAiColors.Primary.value,
                unfocusedBorderColor = ComandaAiColors.Gray400.value,
                errorBorderColor = ComandaAiColors.Error.value,
                focusedLabelColor = ComandaAiColors.Primary.value,
                unfocusedLabelColor = ComandaAiColors.Gray600.value,
                errorLabelColor = ComandaAiColors.Error.value
            )
        )

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))

        // Login Button
        ComandaAiButton(
            text = if (state.isLoading) "Entrando..." else "Continuar",
            onClick = onLogin,
            isEnabled = state.isContinueButtonEnabled,
            variant = ComandaAiButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )

        // Error Message
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(ComandaAiSpacing.Medium.value))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ComandaAiColors.Error.value.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(ComandaAiSpacing.Small.value)
            ) {
                Text(
                    text = error.message,
                    color = ComandaAiColors.Error.value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(ComandaAiSpacing.Medium.value),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(ComandaAiSpacing.xXLarge.value))
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
                color = ComandaAiColors.Primary.value
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
                passwordError = "Senha deve ter pelo menos 4 caracteres"
            ),
            onUsernameChanged = {},
            onPasswordChanged = {},
            onLogin = {},
            onClearError = {}
        )
    }
}