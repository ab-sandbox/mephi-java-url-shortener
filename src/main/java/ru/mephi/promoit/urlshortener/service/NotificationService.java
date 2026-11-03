package ru.mephi.promoit.urlshortener.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.promoit.urlshortener.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.model.Notification;
import ru.mephi.promoit.urlshortener.model.NotificationType;
import ru.mephi.promoit.urlshortener.repository.NotificationRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Clock clock;

    public NotificationService(
            NotificationRepository notificationRepository,
            Clock clock
    ) {
        this.notificationRepository = notificationRepository;
        this.clock = clock;
    }

    @Transactional
    public void notifyLimitReached(UUID userId, String code) {
        createNotification(
                userId,
                code,
                NotificationType.LIMIT_REACHED,
                "Click limit reached for link: " + code
        );
    }

    @Transactional
    public void notifyExpired(UUID userId, String code) {
        createNotification(
                userId,
                code,
                NotificationType.EXPIRED,
                "Link expired: " + code
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(UUID userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id, UUID userId) {
        Notification notification = notificationRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (notification.getReadAt() == null) {
            notification.markAsRead(Instant.now(clock));
        }

        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getShortCode(),
                notification.getType(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private void createNotification(
            UUID userId,
            String code,
            NotificationType type,
            String message
    ) {
        notificationRepository.insertIfAbsent(
                userId,
                code,
                type.name(),
                message,
                Instant.now(clock)
        );
    }
}
