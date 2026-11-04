package ru.mephi.promoit.urlshortener.cli;

import picocli.CommandLine;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.command.CreateLinkCommand;
import ru.mephi.promoit.urlshortener.cli.command.ListLinksCommand;
import ru.mephi.promoit.urlshortener.cli.command.RootCommand;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;

import java.net.URI;

public class Main {

    public static void main(String[] args) {
        UrlShortenerClient client = new UrlShortenerClient(
                URI.create("http://localhost:8080")
        );

        UserSession userSession = new UserSession();

        CommandLine commandLine = new CommandLine(new RootCommand())
                .addSubcommand(new CreateLinkCommand(client, userSession))
                .addSubcommand(new ListLinksCommand(client, userSession));

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}
