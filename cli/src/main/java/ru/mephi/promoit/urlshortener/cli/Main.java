package ru.mephi.promoit.urlshortener.cli;

import picocli.CommandLine;
import ru.mephi.promoit.urlshortener.cli.browser.DesktopBrowserLauncher;
import ru.mephi.promoit.urlshortener.cli.client.UrlShortenerClient;
import ru.mephi.promoit.urlshortener.cli.command.*;
import ru.mephi.promoit.urlshortener.cli.config.CliConfig;
import ru.mephi.promoit.urlshortener.cli.session.UserSession;
import ru.mephi.promoit.urlshortener.cli.shell.InteractiveShell;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        UrlShortenerClient client = new UrlShortenerClient(
                CliConfig.getApiUrl()
        );

        UserSession userSession = new UserSession();

        CommandLine commandLine = createCommandLine(client, userSession);

        RunMode mode = RunMode.fromArgs(args);

        switch (mode) {
            case INTERACTIVE -> {
                Scanner scanner = new Scanner(System.in);

                new InteractiveShell(
                        commandLine,
                        scanner,
                        System.out
                ).run();
            }
            case COMMAND -> {
                int exitCode = commandLine.execute(args);
                System.exit(exitCode);
            }
        }
    }

    private static CommandLine createCommandLine(
            UrlShortenerClient client,
            UserSession userSession
    ) {
        CommandLine notificationsCommand = new CommandLine(
                new NotificationsCommand()
        )
                .addSubcommand(new NotificationsListCommand(client, userSession))
                .addSubcommand(new NotificationsUnreadCommand(client, userSession))
                .addSubcommand(new NotificationsReadCommand(client, userSession));

        return new CommandLine(new RootCommand())
                .addSubcommand(new CreateLinkCommand(client, userSession))
                .addSubcommand(new ListLinksCommand(client, userSession))
                .addSubcommand(new OpenLinkCommand(
                        client,
                        new DesktopBrowserLauncher()
                ))
                .addSubcommand(new UpdateLinkCommand(client, userSession))
                .addSubcommand(new DeleteLinkCommand(client, userSession))
                .addSubcommand(notificationsCommand);
    }
}
