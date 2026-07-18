package com.kyamsg.backend.service;

import com.kyamsg.backend.dto.ChatDtos.*;
import com.kyamsg.backend.entity.Conversation;
import com.kyamsg.backend.entity.Message;
import com.kyamsg.backend.entity.User;
import com.kyamsg.backend.exception.ApiException;
import com.kyamsg.backend.repository.ConversationRepository;
import com.kyamsg.backend.repository.MessageRepository;
import com.kyamsg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse sendMessage(UUID senderId, UUID otherUserId, SendMessageRequest request) {
        if (senderId.equals(otherUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot message yourself");
        }
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Conversation conversation = getOrCreateConversation(senderId, otherUserId);

        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .content(request.content())
                .status(Message.Status.SENT)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        return toMessageResponse(message);
    }

    public ConversationListResponse listConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findAllForUser(userId);

        List<UUID> otherUserIds = conversations.stream()
                .map(c -> c.otherParticipant(userId))
                .toList();
        Map<UUID, User> usersById = userRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<ConversationSummary> summaries = conversations.stream()
                .map(c -> {
                    User otherUser = usersById.get(c.otherParticipant(userId));
                    Message lastMessage = messageRepository
                            .findFirstByConversationIdOrderByCreatedAtDesc(c.getId())
                            .orElse(null);
                    return new ConversationSummary(
                            c.getId().toString(),
                            toUserSummary(otherUser),
                            lastMessage != null ? toMessageResponse(lastMessage) : null
                    );
                })
                .sorted(Comparator.comparing(
                        (ConversationSummary s) -> s.lastMessage() != null ? s.lastMessage().createdAt() : "",
                        Comparator.reverseOrder()
                ))
                .toList();

        return new ConversationListResponse(summaries);
    }

    public MessageListResponse getMessages(UUID userId, UUID conversationId, int page, int size) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));

        if (!conversation.hasParticipant(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Not a participant in this conversation");
        }

        Page<Message> messagePage = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(page, size)
        );

        List<MessageResponse> messages = messagePage.getContent().stream()
                .map(this::toMessageResponse)
                .toList();

        return new MessageListResponse(messages, messagePage.hasNext());
    }

    private Conversation getOrCreateConversation(UUID userA, UUID userB) {
        UUID lower = userA.compareTo(userB) < 0 ? userA : userB;
        UUID higher = userA.compareTo(userB) < 0 ? userB : userA;

        return conversationRepository.findByUserOneIdAndUserTwoId(lower, higher)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .userOneId(lower)
                                .userTwoId(higher)
                                .build()
                ));
    }

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getId().toString(),
                m.getConversationId().toString(),
                m.getSenderId().toString(),
                m.getContent(),
                m.getStatus().name(),
                m.getCreatedAt().toString()
        );
    }

    private ChatUserSummary toUserSummary(User u) {
        if (u == null) return null;
        return new ChatUserSummary(
                u.getId().toString(),
                u.getName(),
                u.getPhoneNumber(),
                u.getProfilePhotoUrl(),
                u.isOnline(),
                u.getLastSeen() != null ? u.getLastSeen().toString() : null
        );
    }
}
