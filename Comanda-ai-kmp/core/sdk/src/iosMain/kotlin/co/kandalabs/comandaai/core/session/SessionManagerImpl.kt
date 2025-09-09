package co.kandalabs.comandaai.sdk.session

import co.kandalabs.comandaai.sdk.cache.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of SessionManager using NSUserDefaults
 */
class SessionManagerImpl(
    private val cacheManager: CacheManager? = null
) : SessionManager {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val SESSION_KEY = "user_session"
    }
    
    override suspend fun saveSession(session: UserSession): Unit = withContext(Dispatchers.Main) {
        val sessionJson = json.encodeToString(session)
        userDefaults.setObject(sessionJson, SESSION_KEY)
        userDefaults.synchronize()
    }
    
    override suspend fun getSession(): UserSession? = withContext(Dispatchers.Main) {
        val sessionJson = userDefaults.stringForKey(SESSION_KEY)
        return@withContext if (sessionJson != null) {
            try {
                json.decodeFromString<UserSession>(sessionJson)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    override suspend fun clearSession(): Unit = withContext(Dispatchers.Main) {
        userDefaults.removeObjectForKey(SESSION_KEY)
        userDefaults.synchronize()
    }
    
    override suspend fun logout(): Unit = withContext(Dispatchers.Main) {
        // Clear session data
        clearSession()
        
        // Clear all cached data
        cacheManager?.clearAllCache()
    }
    
    override suspend fun hasActiveSession(): Boolean = withContext(Dispatchers.Main) {
        userDefaults.stringForKey(SESSION_KEY) != null && getSession() != null
    }
}