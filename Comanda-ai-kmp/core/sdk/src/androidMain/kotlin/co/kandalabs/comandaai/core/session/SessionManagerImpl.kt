package co.kandalabs.comandaai.sdk.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.kandalabs.comandaai.sdk.cache.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Android implementation of SessionManager using EncryptedSharedPreferences
 */
class SessionManagerImpl(
    private val context: Context,
    private val cacheManager: CacheManager? = null
) : SessionManager {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "session_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        private const val SESSION_KEY = "user_session"
    }
    
    override suspend fun saveSession(session: UserSession) = withContext(Dispatchers.IO) {
        val sessionJson = json.encodeToString(session)
        println("SessionManager - Saving session: $session")
        println("SessionManager - Session JSON: $sessionJson")
        sharedPreferences.edit().putString(SESSION_KEY, sessionJson).apply()
    }
    
    override suspend fun getSession(): UserSession? = withContext(Dispatchers.IO) {
        val sessionJson = sharedPreferences.getString(SESSION_KEY, null)
        println("SessionManager - Retrieved JSON: $sessionJson")
        return@withContext if (sessionJson != null) {
            try {
                val session = json.decodeFromString<UserSession>(sessionJson)
                println("SessionManager - Decoded session: $session")
                session
            } catch (e: Exception) {
                println("SessionManager - Error decoding session: ${e.message}")
                null
            }
        } else {
            println("SessionManager - No session found")
            null
        }
    }
    
    override suspend fun clearSession() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(SESSION_KEY).apply()
    }
    
    override suspend fun logout(): Unit = withContext(Dispatchers.IO) {
        // Clear session data
        clearSession()
        
        // Clear all cached data
        cacheManager?.clearAllCache()
    }
    
    override suspend fun hasActiveSession(): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(SESSION_KEY) && getSession() != null
    }
}