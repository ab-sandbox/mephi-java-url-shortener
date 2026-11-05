package ru.mephi.promoit.urlshortener.cli.command;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import ru.mephi.promoit.urlshortener.cli.browser.BrowserLauncher;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenLinkCommandTest {

    @Test
    void shouldOpenShortLink() {
        TestBrowserLauncher browserLauncher = new TestBrowserLauncher();

        UrlShortenerClient client = new UrlShortenerClient(
                URI.create("http://localhost:8080")
        );

        OpenLinkCommand command = new OpenLinkCommand(
                client,
                browserLauncher
        );

        int exitCode = new CommandLine(command).execute("abc123");

        assertEquals(0, exitCode);
        assertEquals(
                URI.create("http://localhost:8080/abc123"),
                browserLauncher.openedUri
        );
    }

    private static class TestBrowserLauncher implements BrowserLauncher {
        private URI openedUri;

        @Override
        public void open(URI uri) {
            openedUri = uri;
        }
    }
}
