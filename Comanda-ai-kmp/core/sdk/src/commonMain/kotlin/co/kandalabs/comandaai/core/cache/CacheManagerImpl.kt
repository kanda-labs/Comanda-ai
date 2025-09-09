package co.kandalabs.comandaai.sdk.cache

/**
 * Implementation of CacheManager for clearing application cache
 */
class CacheManagerImpl : CacheManager {
    
    private val inMemoryCallbacks = mutableListOf<suspend () -> Unit>()
    private val repositories = mutableListOf<CacheableRepository>()
    
    override suspend fun clearAllCache() {
        clearHttpCache()
        clearInMemoryCache()
    }
    
    override suspend fun clearHttpCache() {
        // HttpClient cache clearing is now handled by HttpClientFactory
        // Each new request gets a fresh client, so no manual cache clearing needed
        println("CacheManager - HTTP cache cleared via HttpClientFactory fresh clients")
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