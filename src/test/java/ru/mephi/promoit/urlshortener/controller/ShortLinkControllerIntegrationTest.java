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
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import ru.mephi.promoit.urlshortener.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void shouldNotExceedClickLimitUnderConcurrentRequests() throws Exception {
        String responseBody = mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com",
                                  "clickLimit": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String code = objectMapper.readTree(responseBody)
                .get("code")
                .asText();

        int requestCount = 10;
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(requestCount);

        try {
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                Callable<Integer> task = () -> {
                    ready.countDown();

                    if (!start.await(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Start timeout");
                    }

                    return mockMvc.perform(get("/" + code))
                            .andReturn()
                            .getResponse()
                            .getStatus();
                };

                futures.add(executor.submit(task));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            start.countDown();

            List<Integer> statuses = new ArrayList<>();

            for (Future<Integer> future : futures) {
                statuses.add(future.get(30, TimeUnit.SECONDS));
            }

            assertEquals(1, statuses.stream()
                    .filter(status -> status == 302)
                    .count());

            assertEquals(9, statuses.stream()
                    .filter(status -> status == 410)
                    .count());

            ShortLink link = shortLinkRepository.findByCode(code)
                    .orElseThrow();

            assertEquals(1, link.getClickCount());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldReturnUserLinks() throws Exception {
        String responseBody = mockMvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalUrl": "https://example.com",
                                  "clickLimit": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createdLink = objectMapper.readTree(responseBody);
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
}
