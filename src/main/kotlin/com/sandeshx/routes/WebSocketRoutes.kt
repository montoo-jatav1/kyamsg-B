package com.sandeshx.routes

import com.sandeshx.security.JwtConfig
import com.sandeshx.services.ConnectionRegistry
import com.sandeshx.services.MessageService
import com.sandeshx.services.NotificationService
import com.sandeshx.services.PresenceService
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

@Serializable
private data class WsIncoming(
    val type: String, // "message" | "read" | "typing"
    val receiverId: Long? = null,
    val body: String? = null,
    val imageUrl: String? = null,
    val messageId: Long? = null,
    val isTyping: Boolean? = null
)

fun Route.chatWebSocketRoute() {
    webSocket("/ws/chat") {
        // Auth: client sends `?token=<accessToken>` on the WS handshake URL.
        val token = call.request.queryParameters["token"]
        val userId = try {
            JwtConfig.verifier.verify(token).getClaim("userId").asLong()
        } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid or missing token"))
            return@webSocket
        }

        ConnectionRegistry.register(userId, this)
        PresenceService.markOnline(userId)
        broadcastPresence(userId, true)

        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue
                val text = frame.readText()
                val event = try {
                    Json.decodeFromString<WsIncoming>(text)
                } catch (e: Exception) {
                    continue // ignore malformed frames rather than crashing the socket
                }

                when (event.type) {
                    "message" -> {
                        val receiverId = event.receiverId ?: continue
                        val saved = MessageService.send(userId, receiverId, event.body, event.imageUrl)
                        val payload = Json.encodeToString(
                            mapOf(
                                "type" to "message",
                                "id" to saved.id,
                                "senderId" to saved.senderId,
                                "receiverId" to saved.receiverId,
                                "body" to (saved.body ?: ""),
                                "imageUrl" to (saved.imageUrl ?: ""),
                                "status" to saved.status,
                                "createdAt" to saved.createdAt.toString()
                            )
                        )
                        // echo to sender (multi-device sync) and push to receiver if connected
                        ConnectionRegistry.sessionsFor(userId).forEach { it.send(Frame.Text(payload)) }
                        val receiverSessions = ConnectionRegistry.sessionsFor(receiverId)
                        receiverSessions.forEach { it.send(Frame.Text(payload)) }
                        if (receiverSessions.isNotEmpty()) {
                            MessageService.markDelivered(saved.id)
                        } else {
                            val preview = if (!saved.body.isNullOrBlank()) saved.body else "📷 Photo"
                            NotificationService.notifyNewMessage(receiverId, senderDisplayName = "New message", preview = preview ?: "New message")
                        }
                    }
                    "read" -> {
                        val messageId = event.messageId ?: continue
                        val updated = MessageService.markRead(messageId, userId) ?: continue
                        val payload = Json.encodeToString(
                            mapOf("type" to "read", "messageId" to updated.id.toString(), "readAt" to (updated.readAt ?: 0).toString())
                        )
                        ConnectionRegistry.sessionsFor(updated.senderId).forEach { it.send(Frame.Text(payload)) }
                    }
                    "typing" -> {
                        val receiverId = event.receiverId ?: continue
                        val payload = Json.encodeToString(
                            mapOf("type" to "typing", "userId" to userId.toString(), "isTyping" to (event.isTyping ?: false).toString())
                        )
                        ConnectionRegistry.sessionsFor(receiverId).forEach { it.send(Frame.Text(payload)) }
                    }
                }
            }
        } finally {
            ConnectionRegistry.unregister(userId, this)
            if (!ConnectionRegistry.isConnected(userId)) {
                PresenceService.markOffline(userId)
                broadcastPresence(userId, false)
            }
        }
    }
}

private suspend fun DefaultWebSocketServerSession.broadcastPresence(userId: Long, isOnline: Boolean) {
    // In production, broadcast this only to users who share a conversation with `userId`,
    // fetched from the DB, instead of nothing (kept minimal here — wire up a contacts/chat-list query).
}
