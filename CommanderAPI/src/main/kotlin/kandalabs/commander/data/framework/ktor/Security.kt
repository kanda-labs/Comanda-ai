package kandalabs.commander.infrastructure.framework.ktor

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kandalabs.commander.domain.model.User
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Configures security features for the application
 */
fun Application.configureSecurity() {
    // JWT authentication
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
    
    logger.info { "Security features configured" }
}

object JwtConfig {
    private const val secret = "mySecretKey2024ComandaAi" // In production use environment variables
    private const val issuer = "commander.api"
    private const val audience = "commander.api.users"
    private const val validityInMs = 3_153_600_000_000L // ~100 years
    
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
        .withClaim("userId", user.id)
        .withClaim("role", user.role.name)
        .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
        .sign(Algorithm.HMAC256(secret))
}

