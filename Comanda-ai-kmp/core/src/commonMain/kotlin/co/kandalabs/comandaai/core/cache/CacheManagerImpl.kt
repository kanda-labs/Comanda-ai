package co.kandalabs.comandaai.core.cache

import io.ktor.client.HttpClient

/**
 * Implementation of CacheManager for clearing application cache
 */
class CacheManagerImpl(
    private val httpClient: HttpClient
) : CacheManager {
    
    private val inMemoryCallbacks = mutableListOf<suspend () -> Unit>()
    private val repositories = mutableListOf<CacheableRepository>()
    
    override suspend fun clearAllCache() {
        clearHttpCache()
        clearInMemoryCache()
    }
    
    override suspend fun clearHttpCache() {
        try {
            // Close and recreate HTTP client to clear any internal cache
            httpClient.close()
        } catch (e: Exception) {
            // HTTP client might already be closed, ignore error
            println("CacheManager - HTTP client already closed or error: ${e.message}")
        }
    }
    
    override suspend fun clearInMemoryCache() {
        // Clear registered callbacks
        inMemoryCallbacks.forEach { callback ->
            try {
                callback()
            } catch (e: Exception) {
                // Log error but continue clearing other caches
                println("CacheManager - Error clearing in-memory cache: ${e.message}")
            }
        }
        
        // Clear registered repositories
        repositories.forEach { repository ->
            try {
                repository.clearCache()
            } catch (e: Exception) {
                // Log error but continue clearing other repositories
                println("CacheManager - Error clearing repository cache: ${e.message}")
            }
        }
    }
    
    /**
     * Register a callback to clear repository-specific in-memory cache
     */
    fun registerClearCacheCallback(callback: suspend () -> Unit) {
        inMemoryCallbacks.add(callback)
    }
    
    /**
     * Register a cacheable repository
     */
    fun registerRepository(repository: CacheableRepository) {
        repositories.add(repository)
    }
}