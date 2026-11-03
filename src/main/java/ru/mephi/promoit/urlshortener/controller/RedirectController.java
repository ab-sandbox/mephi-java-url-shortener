package ru.mephi.promoit.urlshortener.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.mephi.promoit.urlshortener.service.ShortLinkService;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortLinkService shortLinkService;

    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }

    @GetMapping("/{code:[0-9a-zA-Z]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        URI originalUrl = shortLinkService.resolveLink(code);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(originalUrl)
                .build();
    }
}
