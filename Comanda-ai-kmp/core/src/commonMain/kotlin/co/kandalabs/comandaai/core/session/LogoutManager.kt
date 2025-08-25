package co.kandalabs.comandaai.core.session

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manager for handling logout operations with a dedicated coroutine scope.
 * This prevents "Parent job is Completed" errors when screen models are cancelled.
 */
object LogoutManager {
    
    // Independent coroutine scope for logout operations
    private val logoutScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    // Callback that will be set by the application
    private var onLogoutCallback: (() -> Unit)? = null
    
    /**
     * Set callback to be called after logout is completed.
     */
    fun setOnLogoutCallback(callback: () -> Unit) {
        onLogoutCallback = callback
    }
    
    /**
     * Performs logout operation safely in an independent coroutine scope.
     * This prevents issues with cancelled screen model scopes.
     */
    fun performLogout(sessionManager: SessionManager) {
        logoutScope.launch {
            try {
                println("LogoutManager: Starting logout process...")
                sessionManager.logout()
                println("LogoutManager: Session cleared, calling logout callback...")
                
                // Call logout callback to trigger navigation
                onLogoutCallback?.invoke()
                println("LogoutManager: Logout completed successfully")
            } catch (e: Exception) {
                println("LogoutManager: Error during logout: ${e.message}")
                // Even if logout fails, try to call callback
                onLogoutCallback?.invoke()
            }
        }
    }
}