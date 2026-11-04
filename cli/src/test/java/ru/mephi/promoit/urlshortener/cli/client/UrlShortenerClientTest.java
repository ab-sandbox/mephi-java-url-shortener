package ru.mephi.promoit.urlshortener.cli.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.urlshortener.cli.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerClientTest {

    private HttpServer server;
    private UrlShortenerClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress("localhost", 0),
                0
        );

        server.start();

        URI baseUrl = URI.create(
                "http://localhost:" + server.getAddress().getPort()
        );

        client = new UrlShortenerClient(baseUrl);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldCreateLink() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();
        AtomicReference<CreateLinkRequest> receivedBody = new AtomicReference<>();

        server.createContext("/api/links", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            receivedBody.set(
                    objectMapper.readValue(body, CreateLinkRequest.class)
            );

            sendResponse(exchange, 201, """
                    {
                      "code": "abc123",
                      "shortUrl": "http://localhost:8080/abc123",
                      "originalUrl": "https://example.com",
                      "clickLimit": 10,
                      "clickCount": 0,
                      "expiresAt": "2026-11-05T12:00:00Z",
                      "userId": "%s"
                    }
                    """.formatted(userId));
        });

        LinkResponse response = client.createLink(
                new CreateLinkRequest("https://example.com", 10),
                userId
        );

        assertEquals("POST", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());
        assertEquals("https://example.com", receivedBody.get().originalUrl());
        assertEquals(10, receivedBody.get().clickLimit());

        assertEquals("abc123", response.code());
        assertEquals(userId, response.userId());
        assertEquals(10, response.clickLimit());
    }

    @Test
    void shouldListLinks() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();

        server.createContext("/api/links", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            sendResponse(exchange, 200, """
                    [
                      {
                        "code": "abc123",
                        "shortUrl": "http://localhost:8080/abc123",
                        "originalUrl": "https://example.com",
                        "clickLimit": 10,
                        "clickCount": 2,
                        "expiresAt": "2026-11-05T12:00:00Z",
                        "userId": "%s"
                      }
                    ]
                    """.formatted(userId));
        });

        List<LinkResponse> links = client.listLinks(userId);

        assertEquals("GET", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());

        assertEquals(1, links.size());
        assertEquals("abc123", links.getFirst().code());
        assertEquals(2, links.getFirst().clickCount());
        assertEquals(userId, links.getFirst().userId());
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        server.createContext("/api/links", exchange ->
                sendResponse(exchange, 200, "[]")
        );

        List<LinkResponse> links = client.listLinks(UUID.randomUUID());

        assertTrue(links.isEmpty());
    }

    @Test
    void shouldHandleServerError() {
        server.createContext("/api/links", exchange ->
                sendResponse(exchange, 500, """
                        {"detail":"Internal server error"}
                        """)
        );

        IOException exception = assertThrows(
                IOException.class,
                () -> client.listLinks(UUID.randomUUID())
        );

        assertTrue(exception.getMessage().contains("HTTP 500"));
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String body
    ) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(status, bytes.length);

        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
