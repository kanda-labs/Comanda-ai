package co.kandalabs.comandaai.core.session

/**
 * Interface for managing user sessions across platform
 */
interface SessionManager {
    /**
     * Save user session data
     */
    suspend fun saveSession(session: UserSession)
    
    /**
     * Get current user session
     */
    suspend fun getSession(): UserSession?
    
    /**
     * Clear current session
     */
    suspend fun clearSession()
    
    /**
     * Clear current session and all cached data
     */
    suspend fun logout()
    
    /**
     * Check if there's an active session
     */
    suspend fun hasActiveSession(): Boolean
}