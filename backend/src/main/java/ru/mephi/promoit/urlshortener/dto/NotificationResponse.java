package ru.mephi.promoit.urlshortener.dto;

import ru.mephi.promoit.urlshortener.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String shortCode,
        NotificationType type,
        String message,
        Instant createdAt,
        Instant readAt
) {
}
