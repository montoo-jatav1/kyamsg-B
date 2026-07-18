package com.kyamsg.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_user_one", columnList = "user_one_id"),
        @Index(name = "idx_conversations_user_two", columnList = "user_two_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    /** Lower of the two participant IDs — keeps (A,B) and (B,A) mapping to the same row. */
    @Column(name = "user_one_id", nullable = false)
    private UUID userOneId;

    @Column(name = "user_two_id", nullable = false)
    private UUID userTwoId;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean hasParticipant(UUID userId) {
        return userOneId.equals(userId) || userTwoId.equals(userId);
    }

    public UUID otherParticipant(UUID userId) {
        return userOneId.equals(userId) ? userTwoId : userOneId;
    }
}
