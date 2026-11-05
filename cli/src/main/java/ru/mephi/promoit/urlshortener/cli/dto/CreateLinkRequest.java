package ru.mephi.promoit.urlshortener.cli.dto;

public record CreateLinkRequest(
        String originalUrl,
        int clickLimit
) {
}
