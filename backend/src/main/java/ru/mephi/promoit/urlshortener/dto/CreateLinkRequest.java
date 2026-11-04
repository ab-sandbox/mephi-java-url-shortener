package ru.mephi.promoit.urlshortener.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLinkRequest(
        @NotBlank String originalUrl,
        @NotNull @Min(1) Integer clickLimit
) {
}
