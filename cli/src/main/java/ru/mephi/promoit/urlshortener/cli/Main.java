package ru.mephi.promoit.urlshortener.cli;

import picocli.CommandLine;
import ru.mephi.promoit.urlshortener.cli.browser.DesktopBrowserLauncher;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.command.*;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.net.URI;

public class Main {

    public static void main(String[] args) {
        UrlShortenerClient client = new UrlShortenerClient(
                URI.create("http://localhost:8080")
        );

        UserSession userSession = new UserSession();

        CommandLine notificationsCommand = new CommandLine(
                new NotificationsCommand()
        )
                .addSubcommand(new NotificationsListCommand(client, userSession))
                .addSubcommand(new NotificationsUnreadCommand(client, userSession))
                .addSubcommand(new NotificationsReadCommand(client, userSession));

        CommandLine commandLine = new CommandLine(new RootCommand())
                .addSubcommand(new CreateLinkCommand(client, userSession))
                .addSubcommand(new ListLinksCommand(client, userSession))
                .addSubcommand(new OpenLinkCommand(
                        client,
                        new DesktopBrowserLauncher()
                ))
                .addSubcommand(new UpdateLinkCommand(client, userSession))
                .addSubcommand(new DeleteLinkCommand(client, userSession))
                .addSubcommand(notificationsCommand);

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
