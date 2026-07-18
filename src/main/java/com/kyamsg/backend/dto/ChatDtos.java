package com.kyamsg.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ChatDtos {

    public record SendMessageRequest(
            @NotBlank @Size(max = 4000) String content
    ) {}

    public record ChatUserSummary(
            String id,
            String name,
            String phoneNumber,
            String profilePhotoUrl,
            boolean online,
            String lastSeen
    ) {}

    public record MessageResponse(
            String id,
            String conversationId,
            String senderId,
            String content,
            String status,
            String createdAt
    ) {}

    public record ConversationSummary(
            String conversationId,
            ChatUserSummary otherUser,
            MessageResponse lastMessage
    ) {}

    public record ConversationListResponse(
            List<ConversationSummary> conversations
    ) {}

    public record MessageListResponse(
            List<MessageResponse> messages,
            boolean hasMore
    ) {}
}
