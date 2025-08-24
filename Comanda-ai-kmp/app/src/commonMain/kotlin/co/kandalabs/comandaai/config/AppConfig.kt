package co.kandalabs.comandaai.config

import co.kandalabs.comandaai.network.NetworkConfig

interface AppConfig {
    val apiBaseUrl: String
    val buildType: String
}

object AppConfigProvider {
    val apiBaseUrl: String = NetworkConfig.currentBaseUrl
    val buildType: String = if (NetworkConfig.currentBaseUrl == NetworkConfig.debugBaseUrl) "debug" else "production"
}