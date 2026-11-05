package ru.mephi.promoit.urlshortener.cli.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.mephi.promoit.urlshortener.cli.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.cli.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.cli.dto.UpdateLinkRequest;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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

    @Test
    void shouldBuildShortUrl() {
        URI shortUrl = client.getShortUrl("abc123");

        assertEquals(
                URI.create("http://localhost:" + server.getAddress().getPort() + "/abc123"),
                shortUrl
        );
    }

    @Test
    void shouldUpdateLink() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();
        AtomicReference<UpdateLinkRequest> receivedBody = new AtomicReference<>();

        server.createContext("/api/links/abc123", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            receivedBody.set(
                    objectMapper.readValue(body, UpdateLinkRequest.class)
            );

            sendResponse(exchange, 200, """
                    {
                      "code": "abc123",
                      "shortUrl": "http://localhost:8080/abc123",
                      "originalUrl": "https://example.com",
                      "clickLimit": 20,
                      "clickCount": 1,
                      "expiresAt": "2026-11-05T12:00:00Z",
                      "userId": "%s"
                    }
                    """.formatted(userId));
        });

        LinkResponse response = client.updateLink(
                "abc123",
                new UpdateLinkRequest(20),
                userId
        );

        assertEquals("PATCH", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());
        assertEquals(20, receivedBody.get().clickLimit());

        assertEquals("abc123", response.code());
        assertEquals(20, response.clickLimit());
        assertEquals(1, response.clickCount());
    }

    @Test
    void shouldDeleteLink() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();

        server.createContext("/api/links/abc123", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });

        client.deleteLink("abc123", userId);

        assertEquals("DELETE", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());
    }

    @Test
    void shouldListNotifications() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();

        server.createContext("/api/notifications", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            sendResponse(exchange, 200, """
                    [
                      {
                        "id": 1,
                        "shortCode": "abc123",
                        "type": "CLICK_LIMIT_REACHED",
                        "message": "Click limit reached",
                        "createdAt": "2026-11-04T10:00:00Z",
                        "readAt": null
                      }
                    ]
                    """);
        });

        List<NotificationResponse> notifications =
                client.listNotifications(userId);

        assertEquals("GET", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());

        assertEquals(1, notifications.size());
        assertEquals(1L, notifications.getFirst().id());
        assertEquals("abc123", notifications.getFirst().shortCode());
        assertNull(notifications.getFirst().readAt());
    }

    @Test
    void shouldListUnreadNotifications() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedPath = new AtomicReference<>();

        server.createContext("/api/notifications/unread", exchange -> {
            receivedPath.set(exchange.getRequestURI().getPath());

            sendResponse(exchange, 200, "[]");
        });

        List<NotificationResponse> notifications =
                client.listUnreadNotifications(userId);

        assertEquals("/api/notifications/unread", receivedPath.get());
        assertTrue(notifications.isEmpty());
    }

    @Test
    void shouldMarkNotificationRead() throws Exception {
        UUID userId = UUID.randomUUID();

        AtomicReference<String> receivedMethod = new AtomicReference<>();
        AtomicReference<String> receivedUserId = new AtomicReference<>();

        server.createContext("/api/notifications/1/read", exchange -> {
            receivedMethod.set(exchange.getRequestMethod());
            receivedUserId.set(
                    exchange.getRequestHeaders().getFirst("X-User-Id")
            );

            sendResponse(exchange, 200, """
                    {
                      "id": 1,
                      "shortCode": "abc123",
                      "type": "CLICK_LIMIT_REACHED",
                      "message": "Click limit reached",
                      "createdAt": "2026-11-04T10:00:00Z",
                      "readAt": "2026-11-04T10:30:00Z"
                    }
                    """);
        });

        NotificationResponse notification =
                client.markNotificationRead(1L, userId);

        assertEquals("PATCH", receivedMethod.get());
        assertEquals(userId.toString(), receivedUserId.get());

        assertEquals(1L, notification.id());
        assertEquals(
                Instant.parse("2026-11-04T10:30:00Z"),
                notification.readAt()
        );
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
