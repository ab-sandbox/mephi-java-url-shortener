package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "List your short links",
        mixinStandardHelpOptions = true
)
public class ListLinksCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    public ListLinksCommand(UrlShortenerClient client, UserSession userSession) {
        this.client = client;
        this.userSession = userSession;
    }

    @Override
    public Integer call() throws Exception {
        UUID userId = userSession.getUserId().orElse(null);

        if (userId == null) {
            System.out.println("No user session found. Create a link first.");
            return 0;
        }

        List<LinkResponse> links = client.listLinks(userId);

        if (links.isEmpty()) {
            System.out.println("You have no short links.");
            return 0;
        }

        for (LinkResponse link : links) {
            System.out.println("Short URL: " + link.shortUrl());
            System.out.println("Original:  " + link.originalUrl());
            System.out.println("Clicks:    " + link.clickCount()
                               + "/" + link.clickLimit());
            System.out.println("Expires:   " + link.expiresAt());
            System.out.println();
        }

        return 0;
    }
}
