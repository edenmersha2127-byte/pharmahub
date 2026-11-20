package com.medicinelocator.notificationservice.infrastructure.persistence.repository;

import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.model.Notification;
import com.medicinelocator.notificationservice.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationRepositoryImpl implements NotificationService {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryImpl(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = toEntity(notification);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId) {
        return jpaRepository.findByIdAndRecipientId(id, recipientId).map(this::toDomain);
    }

    @Override
    public Page<Notification> findByRecipient(UUID recipientId, boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return jpaRepository.findByRecipientIdAndReadFalse(recipientId, pageable)
                    .map(this::toDomain);
        }
        return jpaRepository.findByRecipientId(recipientId, pageable).map(this::toDomain);
    }

    @Override
    public List<Notification> findRecentActivity(UUID recipientId, Collection<NotificationType> types, int limit) {
        return jpaRepository
                .findByRecipientIdAndTypeInOrderByCreatedAtDesc(recipientId, types, PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnread(UUID recipientId) {
        return jpaRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    @Override
    public int markAllAsRead(UUID recipientId) {
        return jpaRepository.markAllAsRead(recipientId, LocalDateTime.now());
    }

    private NotificationEntity toEntity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.getId());
        entity.setRecipientId(notification.getRecipientId());
        entity.setRecipientType(notification.getRecipientType());
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setLink(notification.getLink());
        entity.setRead(notification.isRead());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setReadAt(notification.getReadAt());
        return entity;
    }

    private Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getId(),
                entity.getRecipientId(),
                entity.getRecipientType(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getLink(),
                entity.isRead(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }
}
