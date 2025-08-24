package co.kandalabs.comandaai.network

actual object NetworkConfig {
    
    /**
     * Base IP address from BuildConfig - SINGLE PLACE TO CHANGE
     */
    actual val baseIp: String = BuildConfig.BASE_IP
    
    /**
     * Production API port from BuildConfig
     */
    actual val productionPort: Int = BuildConfig.PRODUCTION_PORT
    
    /**
     * Debug API port from BuildConfig
     */
    actual val debugPort: Int = BuildConfig.DEBUG_PORT
    
    /**
     * Production API base URL (with trailing slash)
     */
    actual val productionBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, productionPort)
    
    /**
     * Debug API base URL (with trailing slash)
     */
    actual val debugBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, debugPort)
    
    /**
     * Current environment's base URL based on BuildConfig.DEBUG
     * - Debug builds automatically use debug port (8082)
     * - Release builds automatically use production port (8081)
     */
    actual val currentBaseUrl: String = if (BuildConfig.DEBUG) {
        debugBaseUrl
    } else {
        productionBaseUrl
    }
}