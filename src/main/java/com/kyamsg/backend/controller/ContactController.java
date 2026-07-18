package com.kyamsg.backend.controller;

import com.kyamsg.backend.dto.ContactDtos.*;
import com.kyamsg.backend.security.CurrentUser;
import com.kyamsg.backend.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/sync")
    public SyncContactsResponse sync(@CurrentUser UUID userId, @Valid @RequestBody SyncContactsRequest request) {
        return contactService.syncContacts(userId, request);
    }

    @GetMapping
    public SyncContactsResponse getContacts(@CurrentUser UUID userId) {
        return contactService.getContacts(userId);
    }
}
