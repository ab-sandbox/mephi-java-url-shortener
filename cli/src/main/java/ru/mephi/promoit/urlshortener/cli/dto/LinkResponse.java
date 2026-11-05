package ru.mephi.promoit.urlshortener.cli.dto;

import java.time.Instant;
import java.util.UUID;

public record LinkResponse(
        String code,
        String shortUrl,
        String originalUrl,
        int clickLimit,
        int clickCount,
        Instant expiresAt,
        UUID userId
) {
}
