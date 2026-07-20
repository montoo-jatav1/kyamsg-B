package com.sandeshx.models

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(val phoneNumber: String)

@Serializable
data class VerifyOtpRequest(val phoneNumber: String, val code: String)

@Serializable
data class AuthResponse(val accessToken: String, val refreshToken: String, val isNewUser: Boolean, val userId: Long)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class UserProfileDto(
    val id: Long,
    val phoneNumber: String,
    val displayName: String?,
    val avatarUrl: String?,
    val isOnline: Boolean,
    val lastSeenAt: Long?
)

@Serializable
data class UpdateProfileRequest(val displayName: String?, val avatarUrl: String?)

@Serializable
data class SendMessageRequest(val receiverId: Long, val body: String? = null, val imageUrl: String? = null)

@Serializable
data class MessageDto(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val body: String?,
    val imageUrl: String?,
    val status: String,
    val createdAt: Long,
    val readAt: Long?
)

@Serializable
data class PresignedUploadResponse(val uploadUrl: String, val fileUrl: String, val objectKey: String)

@Serializable
data class ErrorResponse(val error: String)

@Serializable
sealed class WsEvent {
    @Serializable
    data class NewMessage(val message: MessageDto) : WsEvent()
    @Serializable
    data class Presence(val userId: Long, val isOnline: Boolean, val lastSeenAt: Long?) : WsEvent()
    @Serializable
    data class ReadReceipt(val messageId: Long, val readAt: Long) : WsEvent()
    @Serializable
    data class Typing(val userId: Long, val isTyping: Boolean) : WsEvent()
}
