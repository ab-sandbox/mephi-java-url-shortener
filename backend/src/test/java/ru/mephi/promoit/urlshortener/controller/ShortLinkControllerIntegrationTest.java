package ru.mephi.promoit.urlshortener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;
import ru.mephi.promoit.urlshortener.model.ShortLink;
import ru.mephi.promoit.urlshortener.model.User;
import ru.mephi.promoit.urlshortener.repository.NotificationRepository;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import ru.mephi.promoit.urlshortener.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class ShortLinkControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShortLinkRepository shortLinkRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldCreateLinkAndEnforceClickLimit() throws Exception {
        String responseBody = mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com",
                                  "clickLimit": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalUrl")
                        .value("https://example.com"))
                .andExpect(jsonPath("$.clickLimit").value(2))
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(responseBody);

        String code = response.get("code").asText();

        assertTrue(response.get("shortUrl")
                .asText()
                .endsWith("/" + code));

        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location", "https://example.com"
                ));

        mockMvc.perform(get("/" + code))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        "Location", "https://example.com"
                ));

        mockMvc.perform(get("/" + code))
                .andExpect(status().isGone());

        ShortLink link = shortLinkRepository.findByCode(code)
                .orElseThrow();

        assertEquals(2, link.getClickCount());
    }

    @Test
    void shouldReturnNotFoundForUnknownCode() throws Exception {
        mockMvc.perform(get("/unknown123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "",
                                  "clickLimit": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserLinks() throws Exception {
        JsonNode createdLink = createLink(5);

        String userId = createdLink.get("userId").asText();
        String code = createdLink.get("code").asText();

        mockMvc.perform(get("/api/links")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].code").value(code))
                .andExpect(jsonPath("$[0].originalUrl")
                        .value("https://example.com"))
                .andExpect(jsonPath("$[0].clickLimit").value(5))
                .andExpect(jsonPath("$[0].clickCount").value(0));
    }

    @Test
    void shouldReturnEmptyListForUserWithoutLinks() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setCreatedAt(Instant.now());

        userRepository.save(user);

        mockMvc.perform(get("/api/links")
                        .header("X-User-Id", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/links")
                        .header("X-User-Id", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateClickLimit() throws Exception {
        JsonNode createdLink = createLink(5);

        String code = createdLink.get("code").asText();
        String userId = createdLink.get("userId").asText();

        mockMvc.perform(patch("/api/links/{code}", code)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clickLimit": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.clickLimit").value(10))
                .andExpect(jsonPath("$.clickCount").value(0));

        ShortLink link = shortLinkRepository.findByCode(code)
                .orElseThrow();

        assertEquals(10, link.getClickLimit());
    }

    @Test
    void shouldNotUpdateAnotherUsersLink() throws Exception {
        JsonNode createdLink = createLink(5);
        String code = createdLink.get("code").asText();

        mockMvc.perform(patch("/api/links/{code}", code)
                        .header("X-User-Id", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clickLimit": 10
                                }
                                """))
                .andExpect(status().isNotFound());

        ShortLink link = shortLinkRepository.findByCode(code)
                .orElseThrow();

        assertEquals(5, link.getClickLimit());
    }

    @Test
    void shouldRejectInvalidClickLimit() throws Exception {
        JsonNode createdLink = createLink(5);

        mockMvc.perform(patch("/api/links/{code}",
                        createdLink.get("code").asText())
                        .header("X-User-Id",
                                createdLink.get("userId").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clickLimit": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectClickLimitBelowCurrentCount() throws Exception {
        JsonNode createdLink = createLink(5);

        String code = createdLink.get("code").asText();
        String userId = createdLink.get("userId").asText();

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isFound());

        mockMvc.perform(patch("/api/links/{code}", code)
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clickLimit": 1
                                }
                                """))
                .andExpect(status().isConflict());

        ShortLink link = shortLinkRepository.findByCode(code)
                .orElseThrow();

        assertEquals(5, link.getClickLimit());
        assertEquals(2, link.getClickCount());
    }

    @Test
    void shouldDeleteLink() throws Exception {
        JsonNode link = createLink(5);
        String code = link.get("code").asText();
        UUID userId = UUID.fromString(link.get("userId").asText());

        mockMvc.perform(delete("/api/links/{code}", code)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());

        assertThat(shortLinkRepository.findByCode(code)).isEmpty();

        mockMvc.perform(get("/{code}", code))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteAnotherUsersLink() throws Exception {
        JsonNode link = createLink(5);
        String code = link.get("code").asText();

        mockMvc.perform(delete("/api/links/{code}", code)
                        .header("X-User-Id", UUID.randomUUID()))
                .andExpect(status().isNotFound());

        assertThat(shortLinkRepository.findByCode(code)).isPresent();
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownLink() throws Exception {
        mockMvc.perform(delete("/api/links/{code}", "unknown123")
                        .header("X-User-Id", UUID.randomUUID()))
                .andExpect(status().isNotFound());
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
}
