package co.kandalabs.comandaai.network

/**
 * Centralized network configuration for all API endpoints.
 * This is the SINGLE place to change IPs and ports for the entire application.
 */
expect object NetworkConfig {
    /**
     * Base IP address - change here to affect all modules
     */
    val baseIp: String
    
    /**
     * Production API port
     */
    val productionPort: Int
    
    /**
     * Debug API port  
     */
    val debugPort: Int
    
    /**
     * Production API base URL (with trailing slash)
     */
    val productionBaseUrl: String
    
    /**
     * Debug API base URL (with trailing slash)
     */
    val debugBaseUrl: String
    
    /**
     * Get current environment's base URL based on build type
     */
    val currentBaseUrl: String
}

/**
 * Environment types for network configuration
 */
enum class NetworkEnvironment {
    PRODUCTION,
    DEBUG
}

/**
 * Utility functions for building URLs
 */
object NetworkUtils {
    
    /**
     * Build complete API endpoint URL
     */
    fun buildApiUrl(environment: NetworkEnvironment, endpoint: String): String {
        val baseUrl = when (environment) {
            NetworkEnvironment.PRODUCTION -> NetworkConfig.productionBaseUrl
            NetworkEnvironment.DEBUG -> NetworkConfig.debugBaseUrl
        }
        return "${baseUrl}api/v1/$endpoint"
    }
    
    /**
     * Build complete SSE endpoint URL
     */
    fun buildSseUrl(environment: NetworkEnvironment, endpoint: String): String {
        val baseUrl = when (environment) {
            NetworkEnvironment.PRODUCTION -> NetworkConfig.productionBaseUrl
            NetworkEnvironment.DEBUG -> NetworkConfig.debugBaseUrl
        }
        return "${baseUrl}api/v1/$endpoint"
    }
    
    /**
     * Get base URL with proper trailing slash
     */
    fun formatBaseUrl(ip: String, port: Int): String {
        return "http://$ip:$port/"
    }
}