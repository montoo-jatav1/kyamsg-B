package com.sandeshx

import com.sandeshx.config.DatabaseFactory
import com.sandeshx.config.MinioFactory
import com.sandeshx.models.ErrorResponse
import com.sandeshx.routes.*
import com.sandeshx.security.JwtConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.time.Duration

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    MinioFactory.ensureBucket()

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
    }

    install(CallLogging)

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(20)
        timeout = Duration.ofSeconds(30)
    }

    // Security headers + strict CORS — no wildcard origins in production
    install(CORS) {
        val allowed = System.getenv("ALLOWED_ORIGIN")
        if (allowed != null) allowHost(allowed, schemes = listOf("https")) else anyHost() // dev fallback only
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
    }

    install(RateLimit) {
        register {
            rateLimiter(limit = 60, refillPeriod = Duration.ofMinutes(1))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asLong()
                val type = credential.payload.getClaim("type").asString()
                if (userId != null && type == "access") JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or expired token"))
            }
        }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message ?: "Invalid request"))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Something went wrong"))
        }
    }

    routing {
        authRoutes()
        userRoutes()
        chatRoutes()
        mediaRoutes()
        chatWebSocketRoute()

        get("/health") { call.respond(mapOf("status" to "ok")) }
    }
}
