package co.kandalabs.comandaai.config

import co.kandalabs.comandaai.auth.AuthModule
import co.kandalabs.comandaai.kitchen.KitchenModule
import co.kandalabs.comandaai.config.sqldelight.createDatabase
import co.kandalabs.comandaai.core.cache.CacheManager
import co.kandalabs.comandaai.core.cache.CacheManagerImpl
import co.kandalabs.comandaai.core.logger.ComandaAiLogger
import co.kandalabs.comandaai.core.logger.ComandaAiLoggerImpl
import co.kandalabs.comandaai.core.session.SessionManager
import co.kandalabs.comandaai.data.api.CommanderApi
import co.kandalabs.comandaai.data.api.OrderSSEClient
import co.kandalabs.comandaai.data.repository.ItemsRepositoryImp
import co.kandalabs.comandaai.data.repository.OrderRepositoryImpl
import co.kandalabs.comandaai.data.repository.TablesRepositoryImp
import co.kandalabs.comandaai.domain.repository.ItemsRepository
import co.kandalabs.comandaai.domain.repository.OrderRepository
import co.kandalabs.comandaai.domain.repository.TablesRepository
import co.kandalabs.comandaai.presentation.screens.itemsSelection.BreedsListingViewModel
import co.kandalabs.comandaai.presentation.screens.order.OrderScreenModel
import co.kandalabs.comandaai.presentation.screens.ordercontrol.OrderControlViewModel
import co.kandalabs.comandaai.presentation.screens.ordersline.OrdersLineViewModel
import co.kandalabs.comandaai.presentation.screens.splash.SplashViewModel
import co.kandalabs.comandaai.presentation.screens.payment.PaymentSummaryViewModel
import co.kandalabs.comandaai.presentation.screens.tables.details.TablesDetailsViewModel
import co.kandalabs.comandaai.presentation.screens.tables.listing.TablesViewModel
import co.kandalabs.comandaai.sqldelight.db.ComandaAiDatabase
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private val commonModule = DI.Module("commonModule") {

    bindSingleton<ComandaAiDatabase> {
        createDatabase(instance())
    }
    
    bindSingleton<Json> {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    bindSingleton<HttpClient> {
        HttpClient {
            defaultRequest {
                header("Accept-Encoding", "identity")
                header("Content-Type", "application/json")
                header("Accept", "application/json")
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println("HTTP: $message")
                    }
                }
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(instance<Json>())
            }
            install(SSE)
        }
    }

    bindSingleton<CommanderApi> {
        Ktorfit.Builder()
            .baseUrl(CommanderApi.baseUrl)
            .httpClient(instance<HttpClient>())
            .build()
            .create<CommanderApi>()
    }
    
    bindSingleton<OrderSSEClient> {
        OrderSSEClient(
            httpClient = instance(),
            json = instance(),
            baseUrl = CommanderApi.baseUrl,
            logger = instance()
        )
    }

    bindSingleton<ComandaAiLogger> {
        ComandaAiLoggerImpl()
    }

    bindSingleton<CacheManager> {
        CacheManagerImpl(
            httpClient = instance()
        )
    }

    bindSingleton<ItemsRepository> {
        ItemsRepositoryImp(
            api = instance(),
            logger = instance(),
            dispatcher = Dispatchers.IO,
        )
    }

    bindSingleton<TablesRepository> {
        TablesRepositoryImp(
            commanderApi = instance()
        )
    }

    bindSingleton<OrderRepository> {
        OrderRepositoryImpl(
            commanderApi = instance()
        )
    }

    bindProvider {
        TablesViewModel(
            repository = instance(),
            sessionManager = instance()
        )
    }

    bindProvider {
        TablesDetailsViewModel(
            repository = instance(),
            sessionManager = instance()
        )
    }

    bindProvider {
        PaymentSummaryViewModel(
            repository = instance()
        )
    }

    bindProvider {
        BreedsListingViewModel(repository = instance())
    }

    bindProvider {
        OrderScreenModel(
            itemsRepository = instance(),
            orderRepository = instance()
        )
    }

    bindProvider {
        SplashViewModel(sessionManager = instance())
    }
    
    bindProvider {
        OrdersLineViewModel(
            orderRepository = instance(),
            orderSSEClient = instance(),
            sessionManager = instance()
        )
    }
    
    bindProvider {
        OrderControlViewModel(
            sessionManager = instance(),
            orderRepository = instance()
        )
    }


}

object AppModule {
    fun DI.MainBuilder.initializeKodein() {
        import(commonModule)
        import(AuthModule.authModule)
        import(KitchenModule.kitchenDI)
    }
}

expect val platformDI: DI.Module

val di = DI.lazy {
    import(commonModule)
    import(AuthModule.authModule)
    import(KitchenModule.kitchenDI)
    import(platformDI)
}





