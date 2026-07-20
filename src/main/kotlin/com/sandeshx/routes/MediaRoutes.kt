package com.sandeshx.routes

import com.sandeshx.config.MinioFactory
import com.sandeshx.models.PresignedUploadResponse
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.mediaRoutes() {
    authenticate("auth-jwt") {
        route("/api/media") {
            post("/upload-url") {
                val userId = call.currentUserId()
                val objectKey = "chat-images/$userId/${UUID.randomUUID()}.jpg"
                val uploadUrl = MinioFactory.presignedUploadUrl(objectKey)
                val fileUrl = MinioFactory.presignedDownloadUrl(objectKey)
                call.respond(PresignedUploadResponse(uploadUrl, fileUrl, objectKey))
            }
        }
    }
}
