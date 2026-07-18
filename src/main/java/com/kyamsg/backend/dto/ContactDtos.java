package com.kyamsg.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ContactDtos {

    public record SyncContactItem(
            @NotBlank String phoneNumber,
            @NotBlank String name
    ) {}

    public record SyncContactsRequest(
            @NotEmpty @Valid List<SyncContactItem> contacts
    ) {}

    public record ContactResponse(
            String id,
            String phoneNumber,
            String name,
            String profilePhotoUrl,
            boolean online,
            String lastSeen
    ) {}

    public record SyncContactsResponse(
            List<ContactResponse> contacts
    ) {}
}
