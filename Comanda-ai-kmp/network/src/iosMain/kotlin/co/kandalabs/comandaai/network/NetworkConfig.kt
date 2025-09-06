package co.kandalabs.comandaai.network

actual object NetworkConfig {
    
    /**
     * Base IP address from generated config (local.properties)
     */
    actual val baseIp: String = GeneratedNetworkConfig.BASE_IP
    
    /**
     * Production API port from generated config (local.properties)
     */
    actual val productionPort: Int = GeneratedNetworkConfig.PRODUCTION_PORT
    
    /**
     * Debug API port from generated config (local.properties)
     */
    actual val debugPort: Int = GeneratedNetworkConfig.DEBUG_PORT
    
    /**
     * Production API base URL (with trailing slash)
     */
    actual val productionBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, productionPort)
    
    /**
     * Debug API base URL (with trailing slash)
     */
    actual val debugBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, debugPort)
    
    /**
     * Current environment's base URL - iOS defaults to production
     * TODO: In future, this could be configurable via iOS build configurations
     */
    actual val currentBaseUrl: String = productionBaseUrl
}