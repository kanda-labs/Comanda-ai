package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.core.session.LogoutManager
import co.kandalabs.comandaai.presentation.screens.splash.SplashScreen
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesScreen
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreen
import co.kandalabs.comandaai.core.enums.UserRole
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.theme.ComandaAiTheme
import co.kandalabs.comandaai.presentation.screens.admin.AdminScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Composable
fun ComandaAiApp() {
    val sessionManager: SessionManager by rememberInstance<SessionManager>()

    ComandaAiTheme {
        // Start with splash screen to check for active session
        Navigator(SplashScreen) { navigator ->
            // Configure auth navigation callbacks
            AuthNavigationCallback.setOnLoginSuccess {
                // Navigate based on user role after login
                CoroutineScope(Dispatchers.Main).launch {
                    val session = sessionManager.getSession()
                    when (session?.role) {
                        UserRole.KITCHEN -> navigator.replace(KitchenScreen())
                        UserRole.ADMIN -> navigator.replace(AdminScreen)
                        else -> navigator.replace(TablesScreen)
                    }
                }
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