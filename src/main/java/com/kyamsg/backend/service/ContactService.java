package com.kyamsg.backend.service;

import com.kyamsg.backend.dto.ContactDtos.*;
import com.kyamsg.backend.entity.Contact;
import com.kyamsg.backend.entity.User;
import com.kyamsg.backend.repository.ContactRepository;
import com.kyamsg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    /**
     * Matches the caller's device address book against registered KyaMsg users by phone
     * number, upserts a Contact row for every match (excluding the caller themselves),
     * and returns the caller's full saved-contact list.
     */
    @Transactional
    public SyncContactsResponse syncContacts(UUID ownerId, SyncContactsRequest request) {
        List<String> phoneNumbers = request.contacts().stream()
                .map(SyncContactItem::phoneNumber)
                .distinct()
                .toList();

        List<User> matchedUsers = userRepository.findByPhoneNumberIn(phoneNumbers);

        Map<String, String> nameByPhone = request.contacts().stream()
                .collect(Collectors.toMap(
                        SyncContactItem::phoneNumber,
                        SyncContactItem::name,
                        (existing, replacement) -> existing
                ));

        for (User matchedUser : matchedUsers) {
            if (matchedUser.getId().equals(ownerId)) continue; // don't add yourself

            String savedName = nameByPhone.getOrDefault(
                    matchedUser.getPhoneNumber(),
                    matchedUser.getName() != null ? matchedUser.getName() : matchedUser.getPhoneNumber()
            );

            Contact contact = contactRepository.findByOwnerIdAndContactUser_Id(ownerId, matchedUser.getId())
                    .orElseGet(() -> Contact.builder()
                            .ownerId(ownerId)
                            .contactUser(matchedUser)
                            .build());

            contact.setSavedName(savedName);
            contactRepository.save(contact);
        }

        return getContacts(ownerId);
    }

    public SyncContactsResponse getContacts(UUID ownerId) {
        List<ContactResponse> contacts = contactRepository.findAllByOwnerIdWithUser(ownerId).stream()
                .map(this::toResponse)
                .toList();
        return new SyncContactsResponse(contacts);
    }

    private ContactResponse toResponse(Contact contact) {
        User user = contact.getContactUser();
        return new ContactResponse(
                user.getId().toString(),
                user.getPhoneNumber(),
                contact.getSavedName(),
                user.getProfilePhotoUrl(),
                user.isOnline(),
                user.getLastSeen() != null ? user.getLastSeen().toString() : null
        );
    }
}
