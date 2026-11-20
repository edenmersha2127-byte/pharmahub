package com.medicinelocator.notificationservice.infrastructure.persistence.repository;

import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    Optional<NotificationEntity> findByIdAndRecipientId(UUID id, UUID recipientId);

    Page<NotificationEntity> findByRecipientId(UUID recipientId, Pageable pageable);

    /** Most-recent events of the given types for a recipient (the dashboard activity feed). */
    List<NotificationEntity> findByRecipientIdAndTypeInOrderByCreatedAtDesc(
            UUID recipientId, Collection<NotificationType> types, Pageable pageable);

    Page<NotificationEntity> findByRecipientIdAndReadFalse(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    @Modifying
    @Query("""
            UPDATE NotificationEntity n
            SET n.read = true, n.readAt = :now
            WHERE n.recipientId = :recipientId AND n.read = false
            """)
    int markAllAsRead(@Param("recipientId") UUID recipientId, @Param("now") LocalDateTime now);
}
