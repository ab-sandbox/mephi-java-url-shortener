package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.util.UUID;
import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete a short link",
        mixinStandardHelpOptions = true
)
public class DeleteLinkCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final UserSession userSession;

    @Parameters(
            index = "0",
            description = "Short link code"
    )
    private String code;

    public DeleteLinkCommand(
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

        client.deleteLink(code, userId);

        System.out.println("Link deleted: " + code);

        return 0;
    }
}
