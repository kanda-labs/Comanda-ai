package co.kandalabs.comandaai.network

actual object NetworkConfig {
    actual val baseIp: String = "192.168.1.5"
    actual val productionPort: Int = 8081
    actual val debugPort: Int = 8082
    
    actual val productionBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, productionPort)
    actual val debugBaseUrl: String = NetworkUtils.formatBaseUrl(baseIp, debugPort)
    
    actual val currentBaseUrl: String = when (getBuildVariant()) {
        "debug" -> debugBaseUrl
        "sandbox" -> productionBaseUrl // sandbox usa a mesma porta que production (8081)
        "release" -> productionBaseUrl
        else -> debugBaseUrl // debug é o padrão
    }
    
    private fun getBuildVariant(): String {
        return System.getProperty("buildVariant") ?: "debug"
    }
}