package co.kandalabs.comandaai.sdk.cache

/**
 * Interface for managing application cache during logout
 */
interface CacheManager {
    /**
     * Clear all cached data including HTTP cache and in-memory data
     */
    suspend fun clearAllCache()
    
    /**
     * Clear HTTP client cache
     */
    suspend fun clearHttpCache()
    
    /**
     * Clear in-memory repository caches
     */
    suspend fun clearInMemoryCache()
}