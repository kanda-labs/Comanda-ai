package co.kandalabs.comandaai.presentation.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import co.kandalabs.comandaai.components.ComandaAiTopAppBar
import co.kandalabs.comandaai.theme.ComandaAiTypography
import co.kandalabs.comandaai.tokens.ComandaAiSpacing

public object ItemsManagementScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current

        ItemsManagementScreenContent(
            onNavigateBack = { navigator?.pop() }
        )
    }
}

@Composable
private fun ItemsManagementScreenContent(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        ComandaAiTopAppBar(
            title = "Gerenciar Items",
            onBackOrClose = onNavigateBack,
            icon = Icons.Default.ArrowBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ComandaAiSpacing.Large.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Gerenciamento de Items",
                style = ComandaAiTypography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Esta funcionalidade será implementada em breve.\n\nPermitirá:\n• Adicionar novos items ao cardápio\n• Editar items existentes\n• Definir preços e descrições\n• Gerenciar disponibilidade\n• Associar items às categorias",
                style = ComandaAiTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = ComandaAiSpacing.Medium.value)
            )
        }
    }
}