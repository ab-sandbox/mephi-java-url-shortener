package ru.mephi.promoit.urlshortener.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;
import ru.mephi.promoit.urlshortener.model.NotificationType;
import ru.mephi.promoit.urlshortener.repository.NotificationRepository;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class ShortLinkConcurrencyIntegrationTest {

    private static final int REQUEST_COUNT = 10;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ShortLinkRepository shortLinkRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void shouldNotExceedClickLimitUnderConcurrentRequests() throws Exception {
        JsonNode createdLink = createLink(1);

        String code = createdLink.get("code").asText();
        UUID userId = UUID.fromString(createdLink.get("userId").asText());

        List<Integer> statuses = performConcurrentRequests(code);

        assertRedirectStatuses(statuses);
        assertClickCount(code, 1);
        assertLimitReachedNotification(userId, code);
    }

    private List<Integer> performConcurrentRequests(String code) throws Exception {
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        try {
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < REQUEST_COUNT; i++) {
                futures.add(executor.submit(createRedirectTask(code, ready, start)));
            }

            assertTrue(ready.await(10, TimeUnit.SECONDS));

            start.countDown();

            return collectStatuses(futures);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private Callable<Integer> createRedirectTask(
            String code,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        return () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Start timeout");
            }

            return mockMvc.perform(get("/{code}", code))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        };
    }

    private List<Integer> collectStatuses(
            List<Future<Integer>> futures
    ) throws Exception {
        List<Integer> statuses = new ArrayList<>();

        for (Future<Integer> future : futures) {
            statuses.add(future.get(30, TimeUnit.SECONDS));
        }

        return statuses;
    }

    private void assertRedirectStatuses(List<Integer> statuses) {
        assertEquals(1, statuses.stream()
                .filter(status -> status == 302)
                .count());

        assertEquals(REQUEST_COUNT - 1, statuses.stream()
                .filter(status -> status == 410)
                .count());
    }

    private void assertClickCount(String code, int expectedCount) {
        var link = shortLinkRepository.findByCode(code)
                .orElseThrow();

        assertEquals(expectedCount, link.getClickCount());
    }

    private void assertLimitReachedNotification(UUID userId, String code) {
        var notifications = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId);

        assertEquals(1, notifications.size());
        assertEquals(code, notifications.get(0).getShortCode());
        assertEquals(
                NotificationType.LIMIT_REACHED,
                notifications.get(0).getType()
        );
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
