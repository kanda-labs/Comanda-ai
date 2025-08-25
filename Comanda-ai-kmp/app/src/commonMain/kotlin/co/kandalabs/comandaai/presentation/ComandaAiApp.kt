package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.core.session.LogoutManager
import co.kandalabs.comandaai.presentation.screens.splash.SplashScreen
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesScreen
import co.kandalabs.comandaai.theme.ComandaAiTheme

@Composable
fun ComandaAiApp() {
    ComandaAiTheme {
        // Start with splash screen to check for active session
        Navigator(SplashScreen) { navigator ->
            // Configure auth navigation callbacks
            AuthNavigationCallback.setOnLoginSuccess {
                navigator.replace(TablesScreen)
            }
            
            AuthNavigationCallback.setOnLogoutSuccess {
                // Clear navigation stack and go back to splash/login
                navigator.replaceAll(SplashScreen)
            }
            
            // Configure logout manager callback
            LogoutManager.setOnLogoutCallback {
                // Clear navigation stack and go back to splash/login
                navigator.replaceAll(SplashScreen)
            }
            
            SlideTransition(navigator)
        }
    }
}