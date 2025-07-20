package co.touchlab.dogify.config

import co.touchlab.dogify.config.sqldelight.createDatabase
import co.touchlab.dogify.core.logger.DogifyLogger
import co.touchlab.dogify.core.logger.DogifyLoggerImpl
import co.touchlab.dogify.data.api.CommanderApi
import co.touchlab.dogify.data.api.createCommanderApi
import co.touchlab.dogify.data.repository.ItemsRepositoryImp
import co.touchlab.dogify.data.repository.TablesRepositoryImp
import co.touchlab.dogify.domain.repository.ItemsRepository
import co.touchlab.dogify.domain.repository.TablesRepository
import co.touchlab.dogify.presentation.screens.itemsSelection.BreedsListingViewModel
import co.touchlab.dogify.presentation.screens.tables.details.TablesDetailsViewModel
import co.touchlab.dogify.presentation.screens.tables.listing.TablesViewModel
import co.touchlab.dogify.sqldelight.db.DogifyDatabase
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
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance

private val commonModule = DI.Module("commonModule") {

    bindSingleton<DogifyDatabase> {
        createDatabase(instance())
    }

    bindSingleton<CommanderApi> {
        val httpClient = HttpClient {
            defaultRequest {
                header("Accept-Encoding", "identity")
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
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        Ktorfit.Builder()
            .baseUrl(CommanderApi.baseUrl)
            .httpClient(httpClient)
            .build()
            .createCommanderApi()
    }

    bindSingleton<DogifyLogger> {
        DogifyLoggerImpl()
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

    bindProvider {
        TablesViewModel(repository = instance())
    }

    bindProvider {
        TablesDetailsViewModel(repository = instance())
    }

    bindProvider {
        BreedsListingViewModel(repository = instance())
    }


}

object AppModule {
    fun DI.MainBuilder.initializeKodein() {
        import(commonModule)
    }
}





