package co.kandalabs.comandaai.sdk.cache

/**
 * Interface for repositories that maintain in-memory cache
 */
interface CacheableRepository {
    /**
     * Clear any in-memory cached data
     */
    suspend fun clearCache()
}