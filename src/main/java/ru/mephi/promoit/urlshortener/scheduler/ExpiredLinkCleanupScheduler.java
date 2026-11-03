package ru.mephi.promoit.urlshortener.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.mephi.promoit.urlshortener.service.ExpiredLinkCleanupService;

@Component
public class ExpiredLinkCleanupScheduler {

    private final ExpiredLinkCleanupService cleanupService;

    public ExpiredLinkCleanupScheduler(
            ExpiredLinkCleanupService cleanupService
    ) {
        this.cleanupService = cleanupService;
    }

    @Scheduled(fixedDelayString = "${app.links.cleanup-interval}")
    public void cleanup() {
        cleanupService.cleanupExpiredLinks();
    }
}
