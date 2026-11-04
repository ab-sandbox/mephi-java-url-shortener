package ru.mephi.promoit.urlshortener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;
import ru.mephi.promoit.urlshortener.model.Notification;
import ru.mephi.promoit.urlshortener.repository.NotificationRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldReturnUserNotifications() throws Exception {
        JsonNode link = createLink(1);

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        String responseBody = mockMvc.perform(get("/api/notifications")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode notifications = objectMapper.readTree(responseBody);

        assertThat(notifications.size()).isEqualTo(1);
        assertThat(notifications.get(0).get("shortCode").asText())
                .isEqualTo(code);
        assertThat(notifications.get(0).get("type").asText())
                .isEqualTo("LIMIT_REACHED");
        assertThat(notifications.get(0).get("readAt").isNull())
                .isTrue();
    }

    @Test
    void shouldReturnOnlyUnreadNotifications() throws Exception {
        JsonNode link = createLink(1);

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        JsonNode notifications = getNotifications(userId);
        long notificationId = notifications.get(0).get("id").asLong();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        String responseBody = mockMvc.perform(get("/api/notifications/unread")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode unread = objectMapper.readTree(responseBody);

        assertThat(unread.size()).isZero();
        assertThat(getNotifications(userId).size()).isEqualTo(1);
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        JsonNode link = createLink(1);

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        long notificationId = getNotifications(userId)
                .get(0)
                .get("id")
                .asLong();

        String responseBody = mockMvc.perform(
                        patch("/api/notifications/{id}/read", notificationId)
                                .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode notification = objectMapper.readTree(responseBody);

        assertThat(notification.get("readAt").isNull()).isFalse();

        Notification saved = notificationRepository.findById(notificationId)
                .orElseThrow();

        assertThat(saved.getReadAt()).isNotNull();
    }

    @Test
    void shouldNotAllowAnotherUserToMarkNotificationAsRead() throws Exception {
        JsonNode link = createLink(1);

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        long notificationId = getNotifications(userId)
                .get(0)
                .get("id")
                .asLong();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header("X-User-Id", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        Notification saved = notificationRepository.findById(notificationId)
                .orElseThrow();

        assertThat(saved.getReadAt()).isNull();
    }

    @Test
    void shouldKeepReadTimestampWhenMarkedAsReadAgain() throws Exception {
        JsonNode link = createLink(1);

        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        long notificationId = getNotifications(userId)
                .get(0)
                .get("id")
                .asLong();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        Notification saved = notificationRepository.findById(notificationId)
                .orElseThrow();

        var firstReadAt = saved.getReadAt();

        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notificationId)
                .orElseThrow();

        assertThat(updated.getReadAt()).isEqualTo(firstReadAt);
    }

    private JsonNode createLink(int clickLimit) throws Exception {
        String responseBody = mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com",
                                  "clickLimit": %d
                                }
                                """.formatted(clickLimit)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody);
    }

    private JsonNode getNotifications(UUID userId) throws Exception {
        String responseBody = mockMvc.perform(get("/api/notifications")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(responseBody);
    }
}
