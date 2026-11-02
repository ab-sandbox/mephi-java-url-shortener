package ru.mephi.promoit.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.links")
public record LinkProperties(
        Duration ttl,
        int codeLength
) {
}
