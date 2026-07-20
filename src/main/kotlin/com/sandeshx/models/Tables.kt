package com.sandeshx.models

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : LongIdTable("users") {
    val phoneNumber = varchar("phone_number", 20).uniqueIndex()
    val displayName = varchar("display_name", 80).nullable()
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val lastSeenAt = timestamp("last_seen_at").nullable()
    val isOnline = bool("is_online").default(false)
    val createdAt = timestamp("created_at")
    val fcmToken = varchar("fcm_token", 255).nullable()
}

enum class MessageStatus { SENT, DELIVERED, READ }

object Messages : LongIdTable("messages") {
    val senderId = reference("sender_id", Users)
    val receiverId = reference("receiver_id", Users)
    val body = text("body").nullable()
    val imageUrl = varchar("image_url", 500).nullable()
    val status = enumerationByName("status", 20, MessageStatus::class).default(MessageStatus.SENT)
    val createdAt = timestamp("created_at")
    val deliveredAt = timestamp("delivered_at").nullable()
    val readAt = timestamp("read_at").nullable()
}
