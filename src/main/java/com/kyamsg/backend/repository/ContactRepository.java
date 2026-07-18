package com.kyamsg.backend.repository;

import com.kyamsg.backend.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Query("select c from Contact c join fetch c.contactUser where c.ownerId = :ownerId order by c.savedName asc")
    List<Contact> findAllByOwnerIdWithUser(@Param("ownerId") UUID ownerId);

    Optional<Contact> findByOwnerIdAndContactUser_Id(UUID ownerId, UUID contactUserId);
}
