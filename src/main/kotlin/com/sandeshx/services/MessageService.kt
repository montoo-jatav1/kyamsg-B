package com.sandeshx.services

import com.sandeshx.models.MessageStatus
import com.sandeshx.models.Messages
import com.sandeshx.models.MessageDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

object MessageService {

    private fun rowToDto(row: org.jetbrains.exposed.sql.ResultRow) = MessageDto(
        id = row[Messages.id].value,
        senderId = row[Messages.senderId].value,
        receiverId = row[Messages.receiverId].value,
        body = row[Messages.body],
        imageUrl = row[Messages.imageUrl],
        status = row[Messages.status].name,
        createdAt = row[Messages.createdAt].epochSecond,
        readAt = row[Messages.readAt]?.epochSecond
    )

    suspend fun send(senderId: Long, receiverId: Long, body: String?, imageUrl: String?): MessageDto =
        withContext(Dispatchers.IO) {
            require(!body.isNullOrBlank() || !imageUrl.isNullOrBlank()) { "Message must have text or an image" }
            transaction {
                val id = Messages.insertAndGetId {
                    it[Messages.senderId] = senderId
                    it[Messages.receiverId] = receiverId
                    it[Messages.body] = body
                    it[Messages.imageUrl] = imageUrl
                    it[Messages.status] = MessageStatus.SENT
                    it[Messages.createdAt] = Instant.now()
                }
                rowToDto(Messages.selectAll().where { Messages.id eq id }.single())
            }
        }

    suspend fun history(userA: Long, userB: Long, limit: Int = 50, beforeId: Long? = null): List<MessageDto> =
        withContext(Dispatchers.IO) {
            transaction {
                val convo = ((Messages.senderId eq userA) and (Messages.receiverId eq userB)) or
                        ((Messages.senderId eq userB) and (Messages.receiverId eq userA))
                var query = Messages.selectAll().where { convo }
                if (beforeId != null) {
                    query = Messages.selectAll().where { convo and (Messages.id less beforeId) }
                }
                query.orderBy(Messages.id, SortOrder.DESC).limit(limit).map(::rowToDto).reversed()
            }
        }

    suspend fun markDelivered(messageId: Long) = withContext(Dispatchers.IO) {
        transaction {
            Messages.update({ Messages.id eq messageId and (Messages.status eq MessageStatus.SENT) }) {
                it[status] = MessageStatus.DELIVERED
                it[deliveredAt] = Instant.now()
            }
        }
    }

    suspend fun markRead(messageId: Long, readerId: Long): MessageDto? = withContext(Dispatchers.IO) {
        transaction {
            Messages.update({ (Messages.id eq messageId) and (Messages.receiverId eq readerId) }) {
                it[status] = MessageStatus.READ
                it[readAt] = Instant.now()
            }
            Messages.selectAll().where { Messages.id eq messageId }.singleOrNull()?.let(::rowToDto)
        }
    }
}
