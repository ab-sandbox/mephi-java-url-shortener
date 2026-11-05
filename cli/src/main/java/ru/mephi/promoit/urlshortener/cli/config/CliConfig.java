package ru.mephi.promoit.urlshortener.cli.config;

import java.net.URI;

public final class CliConfig {

    private static final String API_URL_ENV = "URL_SHORTENER_API_URL";
    private static final String DEFAULT_API_URL = "http://localhost:8080";

    private CliConfig() {
    }

    public static URI getApiUrl() {
        return parseApiUrl(System.getenv(API_URL_ENV));
    }

    public static URI parseApiUrl(String value) {
        if (value == null || value.isBlank()) {
            value = DEFAULT_API_URL;
        }

        URI uri = URI.create(value);

        if (!"http".equalsIgnoreCase(uri.getScheme())
            && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(
                    "API URL must use HTTP or HTTPS"
            );
        }

        if (uri.getHost() == null) {
            throw new IllegalArgumentException(
                    "API URL must contain a host"
            );
        }

        return uri;
    }
}
