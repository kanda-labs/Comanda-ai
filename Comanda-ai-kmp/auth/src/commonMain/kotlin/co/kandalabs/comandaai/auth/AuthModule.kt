package co.kandalabs.comandaai.auth

import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.auth.presentation.login.LoginScreen

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
}