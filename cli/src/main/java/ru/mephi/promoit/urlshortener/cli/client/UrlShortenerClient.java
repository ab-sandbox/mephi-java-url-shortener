package ru.mephi.promoit.urlshortener.cli.client;

import ru.mephi.promoit.urlshortener.cli.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.cli.dto.UpdateLinkRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class UrlShortenerClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI baseUrl;

    public UrlShortenerClient(URI baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public LinkResponse createLink(
            CreateLinkRequest request,
            UUID userId
    ) throws IOException, InterruptedException {

        String json = objectMapper.writeValueAsString(request);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(baseUrl.resolve("/api/links"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (userId != null) {
            requestBuilder.header("X-User-Id", userId.toString());
        }

        HttpResponse<String> response = httpClient.send(
                requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 201) {
            throw new IOException(
                    "Failed to create link: HTTP "
                    + response.statusCode()
                    + " - "
                    + response.body()
            );
        }

        return objectMapper.readValue(
                response.body(),
                LinkResponse.class
        );
    }

    public List<LinkResponse> listLinks(UUID userId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUrl.resolve("/api/links"))
                .header("X-User-Id", userId.toString())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to list links: HTTP "
                    + response.statusCode()
                    + " - "
                    + response.body()
            );
        }

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<LinkResponse>>() {
                }
        );
    }

    public URI getShortUrl(String code) {
        return baseUrl.resolve("/" + code);
    }

    public LinkResponse updateLink(
            String code,
            UpdateLinkRequest request,
            UUID userId
    ) throws IOException, InterruptedException {

        String json = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(baseUrl.resolve("/api/links/" + code))
                .header("Content-Type", "application/json")
                .header("X-User-Id", userId.toString())
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Failed to update link: HTTP "
                    + response.statusCode()
                    + " - "
                    + response.body()
            );
        }

        return objectMapper.readValue(response.body(), LinkResponse.class);
    }

    public void deleteLink(String code, UUID userId)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(baseUrl.resolve("/api/links/" + code))
                .header("X-User-Id", userId.toString())
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 204) {
            throw new IOException(
                    "Failed to delete link: HTTP "
                    + response.statusCode()
                    + " - "
                    + response.body()
            );
        }
    }
}
