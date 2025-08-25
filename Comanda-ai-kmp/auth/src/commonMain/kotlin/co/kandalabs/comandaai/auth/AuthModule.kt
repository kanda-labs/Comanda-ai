package co.kandalabs.comandaai.auth

import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.auth.presentation.login.LoginScreen
import co.kandalabs.comandaai.auth.presentation.login.LoginViewModel
import co.kandalabs.comandaai.core.session.SessionManager
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.instance

/**
 * Public API for the Auth module
 * This object provides access to all auth-related screens and functionality
 */
object AuthModule {
    
    /**
     * Returns the login screen
     */
    fun getLoginScreen(): Screen = LoginScreen
    
    /**
     * Auth module version
     */
    const val VERSION = "1.0.0"
    
    /**
     * Auth module DI configuration
     */
    val authModule = DI.Module("authModule") {
        bindProvider<LoginViewModel> {
            LoginViewModel(
                sessionManager = instance()
            )
        }
    }
}