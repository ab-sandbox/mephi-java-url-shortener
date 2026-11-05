package ru.mephi.promoit.urlshortener.cli.command;

import ru.mephi.promoit.urlshortener.cli.dto.NotificationResponse;

public final class NotificationPrinter {

    private NotificationPrinter() {
    }

    public static void print(NotificationResponse notification) {
        System.out.println("ID:       " + notification.id());
        System.out.println("Link:     " + notification.shortCode());
        System.out.println("Type:     " + notification.type());
        System.out.println("Message:  " + notification.message());
        System.out.println("Created:  " + notification.createdAt());
        System.out.println("Status:   " + (
                notification.readAt() == null
                        ? "Unread"
                        : "Read at " + notification.readAt()
        ));
        System.out.println();
    }
}
