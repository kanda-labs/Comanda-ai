package kandalabs.commander.infrastructure.framework.ktor

import io.ktor.server.application.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Configures security features for the application
 */
fun Application.configureSecurity() {
    // Configure security headers
//    install(DefaultHeaders) {
////        // Prevent content from being loaded in an iframe (clickjacking protection)
////        header(HttpHeaders.XFrameOptions, "DENY")
////
////        // Prevent MIME type sniffing exploits
////        header(HttpHeaders.ContentXContentTypeOptions, "nosniff")
//
//        // Require HTTPS connections (useful in production)
//        header(HttpHeaders.StrictTransportSecurity, "max-age=31536000; includeSubDomains")
//
//        // Enable browser XSS protection
//        header("X-XSS-Protection", "1; mode=block")
//
//        // Control permitted sources for content loading (CSP)
//        header(
//            "Content-Security-Policy",
//            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'"
//        )
//    }
    
    // Basic authentication example (disabled by default)
    /*
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to API"
            validate { credentials ->
                // In production, replace with actual user validation logic
                if (credentials.name == "admin" && credentials.password == "password") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
    */
    
    // JWT authentication example (disabled by default)
    /*
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "Access to API"
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or expired token"))
            }
        }
    }
    */
    
    logger.info { "Security features configured" }
}

// Sample JWT configuration (commented out)
/*
object JwtConfig {
    private const val secret = "mySecret" // In production use environment variables
    private const val issuer = "commander.api"
    private const val audience = "commander.api.users"
    private const val validityInMs = 36_000_00 // 1 hour
    
    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    
    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("username", user.name)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(Algorithm.HMAC256(secret))
}
*/

