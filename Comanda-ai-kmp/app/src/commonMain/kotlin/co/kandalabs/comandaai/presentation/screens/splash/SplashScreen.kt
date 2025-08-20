package co.kandalabs.comandaai.presentation.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.kitchen.KitchenModule
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesScreen
import co.kandalabs.comandaai.core.enums.UserRole
import co.kandalabs.comandaai.presentation.screens.admin.AdminScreen

object SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel<SplashViewModel>()
        
        LaunchedEffect(Unit) {
            val hasActiveSession = viewModel.checkActiveSession()
            
            if (hasActiveSession) {
                val userSession = viewModel.getUserSession()
                println("SplashScreen - User session: $userSession")
                println("SplashScreen - User role: ${userSession?.role}")
                when (userSession?.role) {
                    UserRole.KITCHEN -> {
                        println("SplashScreen - Navigating to KitchenScreen")
                        navigator.replace(KitchenModule.getKitchenScreen())
                    }
                    UserRole.WAITER, UserRole.MANAGER -> {
                        println("SplashScreen - Navigating to TablesScreen")
                        navigator.replace(TablesScreen)
                    }

                    UserRole.ADMIN -> {
                        navigator.replace(AdminScreen)
                    }
                    else -> { }
                }
            } else {
                navigator.replace(AuthModule.getLoginScreen())
            }
        }
        
        SplashContent()
    }
}

@Composable
private fun SplashContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Comanda AI",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
        }
    }
}