package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "unread",
        description = "List unread notifications",
        mixinStandardHelpOptions = true
)
public class NotificationsUnreadCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    public NotificationsUnreadCommand(
            UrlShortenerClient client,
            UserSession userSession
    ) {
        this.client = client;
        this.userSession = userSession;
    }

    @Override
    public Integer call() throws Exception {
        UUID userId = userSession.getUserId().orElse(null);

        if (userId == null) {
            System.err.println("No user session found. Create a link first.");
            return 1;
        }

        List<NotificationResponse> notifications =
                client.listUnreadNotifications(userId);

        if (notifications.isEmpty()) {
            System.out.println("No unread notifications.");
            return 0;
        }

        for (NotificationResponse notification : notifications) {
            NotificationPrinter.print(notification);
        }

        return 0;
    }
}
