package ru.mephi.promoit.urlshortener.cli.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String shortCode,
        String type,
        String message,
        Instant createdAt,
        Instant readAt
) {
}
