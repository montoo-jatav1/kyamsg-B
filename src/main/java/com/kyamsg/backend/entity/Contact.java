package com.kyamsg.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contacts", indexes = {
        @Index(name = "idx_contacts_owner", columnList = "owner_id"),
        @Index(name = "idx_contacts_contact_user", columnList = "contact_user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    @GeneratedValue
    private UUID id;

    /** The user who saved this contact (i.e. whose address book this belongs to). */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    /** The KyaMsg user this contact resolves to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id", nullable = false)
    private User contactUser;

    /** Name as saved in the owner's phone address book (not the contact's own profile name). */
    @Column(name = "saved_name", nullable = false, length = 80)
    private String savedName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
