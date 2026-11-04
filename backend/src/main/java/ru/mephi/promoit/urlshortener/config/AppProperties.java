package ru.mephi.promoit.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        URI baseUrl
) {
}
