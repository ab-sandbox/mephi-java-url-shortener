package ru.mephi.promoit.urlshortener.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateLinkRequest(
        @NotNull
        @Min(1)
        Integer clickLimit
) {
}
