package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "create",
        description = "Create a short link",
        mixinStandardHelpOptions = true
)
public class CreateLinkCommand implements Callable<Integer> {

    @Option(
            names = {"-u", "--url"},
            required = true,
            description = "Original URL"
    )
    private String url;

    @Option(
            names = {"-l", "--limit"},
            required = true,
            description = "Maximum number of clicks"
    )
    private int limit;

    @Override
    public Integer call() throws Exception {
        UrlShortenerClient client = new UrlShortenerClient(
                URI.create("http://localhost:8080")
        );

        UserSession userSession = new UserSession();

        UUID userId = userSession.getUserId().orElse(null);

        CreateLinkRequest request = new CreateLinkRequest(url, limit);

        LinkResponse response = client.createLink(request, userId);

        if (userId == null) {
            userSession.saveUserId(response.userId());
        }

        System.out.println("Short URL: " + response.shortUrl());
        System.out.println("User ID:   " + response.userId());
        System.out.println("Expires:   " + response.expiresAt());

        return 0;
    }
}
