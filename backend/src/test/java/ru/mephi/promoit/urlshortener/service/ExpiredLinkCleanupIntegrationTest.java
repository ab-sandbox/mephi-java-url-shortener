package ru.mephi.promoit.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;
import ru.mephi.promoit.urlshortener.model.NotificationType;
import ru.mephi.promoit.urlshortener.repository.NotificationRepository;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class ExpiredLinkCleanupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Clock clock;
    @Autowired
    private ExpiredLinkCleanupService cleanupService;
    @Autowired
    private ShortLinkRepository shortLinkRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE notifications, short_links, users RESTART IDENTITY CASCADE"
        );
    }

    @Test
    void shouldDeleteExpiredLink() throws Exception {
        JsonNode link = createLink();

        String code = link.get("code").asText();
        expireLink(code);

        int deletedCount = cleanupService.cleanupExpiredLinks();

        assertEquals(1, deletedCount);
        assertFalse(shortLinkRepository.findByCode(code).isPresent());
    }

    @Test
    void shouldKeepActiveLink() throws Exception {
        JsonNode link = createLink();

        String code = link.get("code").asText();

        int deletedCount = cleanupService.cleanupExpiredLinks();

        assertEquals(0, deletedCount);
        assertTrue(shortLinkRepository.findByCode(code).isPresent());
    }

    @Test
    void shouldCreateExpirationNotification() throws Exception {
        JsonNode link = createLink();

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        expireLink(code);

        cleanupService.cleanupExpiredLinks();

        var notifications = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId);

        assertEquals(1, notifications.size());
        assertEquals(code, notifications.get(0).getShortCode());
        assertEquals(NotificationType.EXPIRED, notifications.get(0).getType());
    }

    @Test
    void shouldNotCreateDuplicateNotificationsOnRepeatedCleanup() throws Exception {
        JsonNode link = createLink();

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        expireLink(code);

        int firstDeletedCount = cleanupService.cleanupExpiredLinks();
        int secondDeletedCount = cleanupService.cleanupExpiredLinks();

        var notifications = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId);

        assertEquals(1, firstDeletedCount);
        assertEquals(0, secondDeletedCount);
        assertEquals(1, notifications.size());
    }

    private JsonNode createLink() throws Exception {
        String responseBody = mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com",
                                  "clickLimit": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody);
    }

    private void expireLink(String code) {
        Instant now = Instant.now(clock);

        jdbcTemplate.update("""
                        UPDATE short_links
                        SET created_at = ?,
                            expires_at = ?
                        WHERE code = ?
                        """,
                Timestamp.from(now.minusSeconds(3600)),
                Timestamp.from(now.minusSeconds(1)),
                code
        );
    }
}
