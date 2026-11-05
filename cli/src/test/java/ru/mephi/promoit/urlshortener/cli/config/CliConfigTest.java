package ru.mephi.promoit.urlshortener.cli.config;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class CliConfigTest {

    @Test
    void shouldUseDefaultApiUrl() {
        URI uri = CliConfig.parseApiUrl(null);

        assertEquals(
                URI.create("http://localhost:8080"),
                uri
        );
    }

    @Test
    void shouldUseConfiguredApiUrl() {
        URI uri = CliConfig.parseApiUrl(
                "https://example.com:8443"
        );

        assertEquals(
                URI.create("https://example.com:8443"),
                uri
        );
    }

    @Test
    void shouldRejectUnsupportedScheme() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CliConfig.parseApiUrl("ftp://example.com")
        );
    }

    @Test
    void shouldRejectMissingHost() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CliConfig.parseApiUrl("http:///api")
        );
    }
}
