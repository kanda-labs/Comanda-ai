package co.kandalabs.comandaai.presentation.screens.splash

import cafe.adriel.voyager.core.model.ScreenModel
import co.kandalabs.comandaai.core.session.SessionManager

class SplashViewModel(
    private val sessionManager: SessionManager
) : ScreenModel {
    
    suspend fun checkActiveSession(): Boolean {
        return try {
            sessionManager.hasActiveSession()
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getUserSession() = sessionManager.getSession()
}