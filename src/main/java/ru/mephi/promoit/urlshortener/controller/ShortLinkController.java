package ru.mephi.promoit.urlshortener.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.mephi.promoit.urlshortener.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.service.ShortLinkService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/links")
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse createLink(
            @Valid @RequestBody CreateLinkRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId
    ) {
        return shortLinkService.createLink(request, userId);
    }

    @GetMapping
    public List<LinkResponse> getUserLinks(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return shortLinkService.getUserLinks(userId);
    }
}
