package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List all notifications",
        mixinStandardHelpOptions = true
)
public class NotificationsListCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    public NotificationsListCommand(
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
                client.listNotifications(userId);

        if (notifications.isEmpty()) {
            System.out.println("No notifications found.");
            return 0;
        }

        for (NotificationResponse notification : notifications) {
            NotificationPrinter.print(notification);
        }

        return 0;
    }
}
