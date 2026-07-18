package com.kyamsg.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_phone", columnList = "phone_number", unique = true),
        @Index(name = "idx_users_username", columnList = "username", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "name", length = 80)
    private String name;

    @Column(name = "username", unique = true, length = 40)
    private String username;

    @Column(name = "about", length = 160)
    private String about;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "recovery_email")
    private String recoveryEmail;

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Column(name = "is_online", nullable = false)
    @Builder.Default
    private boolean online = false;

    @Column(name = "last_seen_privacy", length = 20)
    @Builder.Default
    private String lastSeenPrivacy = "everyone";

    @Column(name = "profile_photo_privacy", length = 20)
    @Builder.Default
    private String profilePhotoPrivacy = "everyone";

    @Column(name = "about_privacy", length = 20)
    @Builder.Default
    private String aboutPrivacy = "everyone";

    @Column(name = "read_receipts_enabled", nullable = false)
    @Builder.Default
    private boolean readReceiptsEnabled = true;

    /** Encrypted at rest — see AttributeEncryptor. Stores the user's own Gemini API key for the Myra AI feature. */
    @Column(name = "myra_ai_key_encrypted", columnDefinition = "text")
    private String myraAiKeyEncrypted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.lastSeen == null) this.lastSeen = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
