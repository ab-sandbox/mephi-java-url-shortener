package ru.mephi.promoit.urlshortener.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.mephi.promoit.urlshortener.config.TestcontainersConfig;
import ru.mephi.promoit.urlshortener.model.ShortLink;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;

import java.util.ArrayList;
import java.util.List;
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
}
