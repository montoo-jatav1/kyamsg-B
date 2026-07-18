package com.kyamsg.backend.controller;

import com.kyamsg.backend.dto.ChatDtos.*;
import com.kyamsg.backend.security.CurrentUser;
import com.kyamsg.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ConversationListResponse listConversations(@CurrentUser UUID userId) {
        return chatService.listConversations(userId);
    }

    @PostMapping("/{otherUserId}/messages")
    public MessageResponse sendMessage(
            @CurrentUser UUID userId,
            @PathVariable UUID otherUserId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        return chatService.sendMessage(userId, otherUserId, request);
    }

    @GetMapping("/{conversationId}/messages")
    public MessageListResponse getMessages(
            @CurrentUser UUID userId,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return chatService.getMessages(userId, conversationId, page, size);
    }
}
