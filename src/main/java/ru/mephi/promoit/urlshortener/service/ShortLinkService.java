package ru.mephi.promoit.urlshortener.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.promoit.urlshortener.config.AppProperties;
import ru.mephi.promoit.urlshortener.config.LinkProperties;
import ru.mephi.promoit.urlshortener.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.dto.UpdateLinkRequest;
import ru.mephi.promoit.urlshortener.exception.LinkConflictException;
import ru.mephi.promoit.urlshortener.exception.LinkUnavailableException;
import ru.mephi.promoit.urlshortener.model.ShortLink;
import ru.mephi.promoit.urlshortener.model.User;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import ru.mephi.promoit.urlshortener.repository.UserRepository;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ShortLinkService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;
    private final ShortLinkRepository shortLinkRepository;
    private final ShortCodeGenerator codeGenerator;
    private final LinkProperties linkProperties;
    private final AppProperties appProperties;
    private final Clock clock;

    public ShortLinkService(
            UserRepository userRepository,
            ShortLinkRepository shortLinkRepository,
            ShortCodeGenerator codeGenerator,
            LinkProperties linkProperties, AppProperties appProperties,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.shortLinkRepository = shortLinkRepository;
        this.codeGenerator = codeGenerator;
        this.linkProperties = linkProperties;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    @Transactional
    public LinkResponse createLink(
            CreateLinkRequest request,
            UUID userId
    ) {
        validateUrl(request.originalUrl());

        Instant now = clock.instant();

        User user = resolveUser(userId, now);

        String code = generateUniqueCode();

        ShortLink link = new ShortLink(
                user,
                code,
                request.originalUrl(),
                request.clickLimit(),
                now,
                now.plus(linkProperties.ttl())
        );

        ShortLink savedLink = shortLinkRepository.save(link);

        return toResponse(savedLink);
    }

    @Transactional
    public URI resolveLink(String code) {
        ShortLink link = shortLinkRepository.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException(
                        "Short link not found: " + code
                ));

        Instant now = clock.instant();

        if (link.isExpired(now) || link.isLimitReached()) {
            throw new LinkUnavailableException(
                    "Short link is expired or click limit is reached"
            );
        }

        int updatedRows = shortLinkRepository.incrementClickCount(code, now);

        if (updatedRows == 0) {
            throw new LinkUnavailableException(
                    "Short link is expired or click limit is reached"
            );
        }

        return URI.create(link.getOriginalUrl());
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> getUserLinks(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("User not found");
        }

        return shortLinkRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LinkResponse updateClickLimit(
            String code,
            UUID userId,
            UpdateLinkRequest request
    ) {
        ShortLink link = shortLinkRepository.findByCode(code)
                .filter(existing -> existing.getUser().getId().equals(userId))
                .orElseThrow(() -> new NoSuchElementException("Link not found"));

        int updatedRows = shortLinkRepository.updateClickLimit(
                code,
                userId,
                request.clickLimit()
        );

        if (updatedRows == 0) {
            throw new LinkConflictException(
                    "Click limit cannot be less than the current click count"
            );
        }

        // Обновляем состояние после native SQL UPDATE.
        entityManager.refresh(link);

        return toResponse(link);
    }

    private User resolveUser(UUID userId, Instant now) {
        if (userId == null) {
            User user = new User(UUID.randomUUID(), now);
            return userRepository.save(user);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId
                ));
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String code = codeGenerator.generate(
                    linkProperties.codeLength()
            );

            if (!shortLinkRepository.existsByCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException(
                "Failed to generate unique short code"
        );
    }

    private void validateUrl(String originalUrl) {
        try {
            URI uri = URI.create(originalUrl);

            String scheme = uri.getScheme();

            if (scheme == null
                || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))
                || uri.getHost() == null) {
                throw new IllegalArgumentException(
                        "Only absolute HTTP and HTTPS URLs are allowed"
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid original URL: " + originalUrl,
                    exception
            );
        }
    }

    private LinkResponse toResponse(ShortLink link) {
        return new LinkResponse(
                link.getCode(),
                appProperties.baseUrl().resolve("/" + link.getCode()).toString(),
                link.getOriginalUrl(),
                link.getClickLimit(),
                link.getClickCount(),
                link.getExpiresAt(),
                link.getUser().getId()
        );
    }
}
