package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import ru.mephi.promoit.urlshortener.cli.browser.BrowserLauncher;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;

import java.net.URI;
import java.util.concurrent.Callable;

@Command(
        name = "open",
        description = "Open a short link in the browser",
        mixinStandardHelpOptions = true
)
public class OpenLinkCommand implements Callable<Integer> {

    private final UrlShortenerClient client;
    private final BrowserLauncher browserLauncher;

    @Parameters(
            index = "0",
            description = "Short link code"
    )
    private String code;

    public OpenLinkCommand(
            UrlShortenerClient client,
            BrowserLauncher browserLauncher
    ) {
        this.client = client;
        this.browserLauncher = browserLauncher;
    }

    @Override
    public Integer call() throws Exception {
        URI shortUrl = client.getShortUrl(code);

        browserLauncher.open(shortUrl);

        System.out.println("Opening: " + shortUrl);

        return 0;
    }
}
