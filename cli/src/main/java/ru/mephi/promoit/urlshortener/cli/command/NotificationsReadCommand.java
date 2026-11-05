package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.NotificationResponse;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "read",
        description = "Mark a notification as read",
        mixinStandardHelpOptions = true
)
public class NotificationsReadCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    @Parameters(
            index = "0",
            description = "Notification ID"
    )
    private long id;

    public NotificationsReadCommand(
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

        NotificationResponse notification =
                client.markNotificationRead(id, userId);

        System.out.println("Notification marked as read:");
        NotificationPrinter.print(notification);

        return 0;
    }
}
