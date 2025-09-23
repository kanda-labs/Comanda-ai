package co.kandalabs.comandaai.config

import co.kandalabs.comandaai.config.sqldelight.createDatabase
import co.kandalabs.comandaai.core.di.sdkModule
import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.sdk.cache.CacheManager
import co.kandalabs.comandaai.sdk.cache.CacheManagerImpl
import co.kandalabs.comandaai.presentation.screens.splash.SplashViewModel
import co.kandalabs.comandaai.sqldelight.db.ComandaAiDatabase
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

object AppModule {
    val appModule = DI.Module("appModule") {
        import(sdkModule)
        
        // Database
        bindSingleton<ComandaAiDatabase> {
            createDatabase(instance())
        }

        // Cache
        bindSingleton<CacheManager> {
            CacheManagerImpl()
        }

        // JSON
        bindSingleton<Json> {
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }
        }

        // HTTP Client
        bindSingleton<HttpClient> {
            HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            instance<ComandaAiLogger>().d("HTTP: $message")
                        }
                    }
                    level = LogLevel.INFO
                }
                defaultRequest {
                    url(AppConfigProvider.apiBaseUrl)
                    header("Accept", "application/json")
                    header("Content-Type", "application/json")
                }
            }
        }


        // Ktorfit
        bindSingleton<Ktorfit> {
            Ktorfit.Builder()
                .httpClient(instance<HttpClient>())
                .build()
        }

        // ViewModels
        bindProvider<SplashViewModel> {
            SplashViewModel(instance())
        }
    }
}

// Platform DI - expect/actual pattern
expect val platformDI: DI.Module