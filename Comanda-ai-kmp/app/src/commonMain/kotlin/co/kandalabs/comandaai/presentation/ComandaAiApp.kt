package co.kandalabs.comandaai.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import co.kandalabs.comandaai.features.attendance.presentation.screens.admin.AdminScreen
import co.kandalabs.comandaai.presentation.screens.splash.SplashScreen
import co.kandalabs.comandaai.features.attendance.presentation.screens.tables.listing.TablesScreen
import co.kandalabs.comandaai.auth.navigation.AuthNavigationCallback
import co.kandalabs.comandaai.core.session.LogoutManager
import co.kandalabs.comandaai.kitchen.presentation.KitchenScreen
import co.kandalabs.comandaai.core.enums.UserRole
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.theme.ComandaAiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.compose.rememberInstance

@Composable
fun ComandaAiApp() {
    val sessionManager: SessionManager by rememberInstance<SessionManager>()

    ComandaAiTheme {
        Navigator(SplashScreen) { navigator ->
            AuthNavigationCallback.setOnLoginSuccess {
                CoroutineScope(Dispatchers.Main).launch {
                    val session = sessionManager.getSession()
                    when (session?.role) {
                        UserRole.KITCHEN -> navigator.replaceAll(KitchenScreen)
                        UserRole.ADMIN -> navigator.replaceAll(AdminScreen)
                        else -> navigator.replaceAll(TablesScreen)
                    }
                }
            }

            AuthNavigationCallback.setOnLogoutSuccess {
                navigator.replaceAll(SplashScreen)
            }

            LogoutManager.setOnLogoutCallback {
                navigator.replaceAll(SplashScreen)
            }

            SlideTransition(navigator = navigator)
        }
    }
}