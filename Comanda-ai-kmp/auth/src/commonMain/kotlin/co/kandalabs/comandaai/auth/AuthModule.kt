package co.kandalabs.comandaai.auth

import cafe.adriel.voyager.core.screen.Screen
import co.kandalabs.comandaai.auth.data.AuthRepositoryImpl
import co.kandalabs.comandaai.auth.domain.AuthRepository
import co.kandalabs.comandaai.auth.presentation.login.LoginScreen
import co.kandalabs.comandaai.auth.presentation.login.LoginViewModel
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.network.NetworkConfig
import co.kandalabs.comandaai.network.NetworkUtils
import io.ktor.client.HttpClient
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
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
     * Auth module DI configuration - now uses centralized network config
     */
    val authModule = DI.Module("authModule") {
        bindSingleton<AuthRepository> {
            AuthRepositoryImpl(
                httpClient = instance<HttpClient>(),
                baseUrl = NetworkUtils.buildApiUrl(
                    environment = if (NetworkConfig.currentBaseUrl == NetworkConfig.debugBaseUrl) 
                        co.kandalabs.comandaai.network.NetworkEnvironment.DEBUG 
                    else 
                        co.kandalabs.comandaai.network.NetworkEnvironment.PRODUCTION,
                    endpoint = ""
                ).removeSuffix("/")
            )
        }
        
        bindProvider<LoginViewModel> {
            LoginViewModel(
                authRepository = instance(),
                sessionManager = instance()
            )
        }
    }
}