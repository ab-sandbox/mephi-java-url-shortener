package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "notifications",
        description = "Manage link notifications",
        mixinStandardHelpOptions = true
)
public class NotificationsCommand implements Runnable {

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }
}
