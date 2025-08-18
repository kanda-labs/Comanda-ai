package kandalabs.commander.application

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.Koin
import org.koin.ktor.ext.inject
import org.koin.logger.slf4jLogger
import org.koin.dsl.module
import mu.KotlinLogging

import kandalabs.commander.application.config.DatabaseConfig
import kandalabs.commander.data.model.sqlModels.BillTable
import kandalabs.commander.data.model.sqlModels.ItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemTable
import kandalabs.commander.data.model.sqlModels.OrderItemStatusTable
import kandalabs.commander.data.model.sqlModels.OrderTable
import kandalabs.commander.data.model.sqlModels.TableTable
import kandalabs.commander.data.model.sqlModels.UserTable
import kandalabs.commander.domain.repository.UserRepository
import kandalabs.commander.domain.repository.TableRepository
import kandalabs.commander.domain.repository.BillRepository
import kandalabs.commander.domain.repository.ItemRepository
import kandalabs.commander.domain.repository.OrderRepository
import kandalabs.commander.domain.service.UserService
import kandalabs.commander.domain.service.TableService
import kandalabs.commander.domain.service.BillService
import kandalabs.commander.domain.service.ItemService
import kandalabs.commander.domain.service.OrderService
import kandalabs.commander.domain.service.KitchenService
import kandalabs.commander.domain.service.KitchenServiceImpl
import kandalabs.commander.infrastructure.framework.ktor.configurePlugins
import kandalabs.commander.infrastructure.framework.ktor.configureSecurity
import kandalabs.commander.data.repository.UserRepositoryImpl
import kandalabs.commander.data.repository.TableRepositoryImpl
import kandalabs.commander.data.repository.BillRepositoryImpl
import kandalabs.commander.data.repository.ItemRepositoryImpl
import kandalabs.commander.data.repository.OrderRepositoryImpl
import kandalabs.commander.presentation.routes.userRoutes
import kandalabs.commander.presentation.routes.tableRoutes
import kandalabs.commander.presentation.routes.billRoutes
import kandalabs.commander.presentation.routes.itemRoutes
import kandalabs.commander.presentation.routes.orderRoutes
import kandalabs.commander.presentation.routes.orderSSERoutes
import kandalabs.commander.presentation.routes.authRoutes
import kandalabs.commander.presentation.routes.kitchenRoutes
import kotlinx.serialization.json.Json
import mu.KLogger

private val logger = KotlinLogging.logger {}

/**
 * Application entry point
 */
fun main() {
    logger.info { "Starting CommanderAPI application" }

    try {
        DatabaseConfig.init()
        val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
        val host = System.getenv("HOST") ?: "0.0.0.0"

        logger.info { "Starting server on $host:$port" }

        // Start the server
        embeddedServer(
            factory = Netty,
            port = port,
            host = host,
            module = Application::module
        ).start(wait = true)
    } catch (e: Exception) {
        logger.error(e) { "Failed to start application: ${e.message}" }
        throw e
    }
}

/**
 * Application module configuration
 */
fun Application.module() {
    logger.info { "Configuring application module" }

    // Install Koin for dependency injection
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    configurePlugins()
    configureSecurity()
    configureRouting()

    logger.info { "Application configuration completed" }
}

/**
 * Configure all application routes
 */
fun Application.configureRouting() {
    val userService by inject<UserService>()
    val tableService by inject<TableService>()
    val billService by inject<BillService>()
    val itemService by inject<ItemService>()
    val orderService by inject<OrderService>()
    val kitchenService by inject<KitchenService>()

    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK")
        }

        // API version endpoint
        get("/version") {
            call.respondText("Commander API v1.0.0")
        }

        // Application routes
        route("/api/v1") {
            authRoutes(userService)
            userRoutes(userService)
            tableRoutes(tableService, orderService)
            billRoutes(billService, tableService)
            itemRoutes(itemService)
            orderRoutes(orderService)
            orderSSERoutes(orderService)
            kitchenRoutes(kitchenService)
        }
    }
}

/**
 * Koin dependency injection module
 */
private val appModule = module {
    single<UserTable> { UserTable }
    single<BillTable> { BillTable }
    single<TableTable> { TableTable }
    single<OrderTable> { OrderTable }
    single<OrderItemTable> { OrderItemTable }
    single<OrderItemStatusTable> { OrderItemStatusTable }
    single<ItemTable> { ItemTable }
    single<KLogger> { KotlinLogging.logger {} }
    single<Json> { Json { prettyPrint = true; ignoreUnknownKeys = true } }


    single<UserRepository> { UserRepositoryImpl(userTable = get(), logger = get()) }
    single<TableRepository> { TableRepositoryImpl(tableTable = get(), logger = get()) }
    single<BillRepository> { BillRepositoryImpl(
        billTable = get(), logger = get(),
        orderTable = get()
    ) }
    single<ItemRepository> { ItemRepositoryImpl(itemTable = get(), logger = get()) }
    single<OrderRepository> { OrderRepositoryImpl(
        orderTable = get(), logger = get(),
        orderItemTable = get(), orderItemStatusTable = get()
    ) }

    single { UserService(get()) }
    single { TableService(get()) }
    single { BillService(get()) }
    single { ItemService(get()) }
    single { OrderService(get()) }
    single<KitchenService> { KitchenServiceImpl(get()) }
}
