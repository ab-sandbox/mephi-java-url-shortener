package ru.mephi.promoit.urlshortener.controller;

import org.springframework.web.bind.annotation.*;
import ru.mephi.promoit.urlshortener.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.service.NotificationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread")
    public List<NotificationResponse> getUnreadNotifications(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return notificationService.getUnreadNotifications(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return notificationService.markAsRead(id, userId);
    }
}
