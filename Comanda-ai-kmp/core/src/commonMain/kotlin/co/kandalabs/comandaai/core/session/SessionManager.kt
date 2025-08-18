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
     * Check if there's an active session
     */
    suspend fun hasActiveSession(): Boolean
}