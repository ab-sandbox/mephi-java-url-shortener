package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.cli.dto.UpdateLinkRequest;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "update",
        description = "Update the click limit of a short link",
        mixinStandardHelpOptions = true
)
public class UpdateLinkCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    @Parameters(
            index = "0",
            description = "Short link code"
    )
    private String code;

    @Option(
            names = {"-l", "--limit"},
            required = true,
            description = "New maximum number of clicks"
    )
    private int limit;

    public UpdateLinkCommand(
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

        UpdateLinkRequest request = new UpdateLinkRequest(limit);

        LinkResponse response = client.updateLink(code, request, userId);

        System.out.println("Short URL: " + response.shortUrl());
        System.out.println("Click limit updated: " + response.clickLimit());

        return 0;
    }
}
