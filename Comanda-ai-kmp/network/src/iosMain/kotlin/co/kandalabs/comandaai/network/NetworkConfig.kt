package co.kandalabs.comandaai.network

actual object NetworkConfig {
    
    /**
     * Base IP address - SINGLE PLACE TO CHANGE FOR iOS
     */
    actual val baseIp: String = "192.168.2.200"
    
    /**
     * Production API port
     */
    actual val productionPort: Int = 8081
    
    /**
     * Debug API port
     */
    actual val debugPort: Int = 8081
    
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