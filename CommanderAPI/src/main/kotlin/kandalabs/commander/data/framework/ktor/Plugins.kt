package kandalabs.commander.infrastructure.framework.ktor

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.*
import io.ktor.server.sse.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.slf4j.event.Level

private val logger = KotlinLogging.logger {}

/**
 * Configures all Ktor plugins for the application
 */
fun Application.configurePlugins() {
    // Configure Content Negotiation with JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        })
    }
    
    // Configure Server-Sent Events
    install(SSE)
    
    // Configure CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        
        // In production, this should be restricted to specific hosts
        anyHost()
        
        // Allow credentials (cookies, etc.)
        allowCredentials = true
        
        // Cache preflight requests for 1 hour
        maxAgeInSeconds = 3600
    }
    
    // Configure call logging
    install(CallLogging) {
        level = Level.INFO
        
        // Add custom logging format
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val path = call.request.path()
            val userAgent = call.request.headers["User-Agent"]
            val clientIp = call.request.origin.remoteHost
            
            "Request: $httpMethod $path from $clientIp | Status: $status | Agent: $userAgent"
        }
    }
    
    // Add default headers to all responses
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
        header("X-Content-Type-Options", "nosniff")
    }
    
    // Configure global error handling
    install(StatusPages) {
        // Handle validation exceptions
        exception<IllegalArgumentException> { call, cause ->
            logger.warn { "Validation error: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to (cause.message ?: "Validation error"))
            )
        }
        
        // Handle not found exceptions
        exception<NoSuchElementException> { call, cause ->
            logger.warn { "Resource not found: ${cause.message}" }
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to (cause.message ?: "Resource not found"))
            )
        }
        
        // Handle general exceptions
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception occurred" }
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error")
            )
        }
        
        // Handle 404 for undefined routes
        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Route not found: ${call.request.path()}")
            )
        }
    }
    
    // Configure OpenAPI documentation
//   documentation install(OpenAPI) {
//        // Path to OpenAPI spec file (will be generated)
//        path = "openapi/documentation.yaml"
//
//        // Optional configuration
//        info {
//            title = "Commander API"
//            version = "1.0.0"
//            description = "API for Commander application"
//            contact {
//                name = "Support"
//                email = "support@example.com"
//            }
//        }
//    }
//
//    // Configure Swagger UI
//    install(SwaggerUI) {
//        swaggerUrl = "openapi/documentation.yaml"
//        version = "4.15.5" // Swagger UI version
//    }
}

