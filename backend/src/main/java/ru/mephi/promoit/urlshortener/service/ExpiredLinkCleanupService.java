package ru.mephi.promoit.urlshortener.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.promoit.urlshortener.model.ShortLink;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class ExpiredLinkCleanupService {

    private final ShortLinkRepository shortLinkRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public ExpiredLinkCleanupService(
            ShortLinkRepository shortLinkRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this.shortLinkRepository = shortLinkRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Transactional
    public int cleanupExpiredLinks() {
        Instant now = Instant.now(clock);

        List<ShortLink> expiredLinks =
                shortLinkRepository.findByExpiresAtLessThanEqual(now);

        for (ShortLink link : expiredLinks) {
            notificationService.notifyExpired(
                    link.getUser().getId(),
                    link.getCode()
            );
        }

        shortLinkRepository.deleteAll(expiredLinks);

        return expiredLinks.size();
    }
}
