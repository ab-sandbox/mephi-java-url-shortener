package ru.mephi.promoit.urlshortener.cli;

import picocli.CommandLine;
import ru.mephi.promoit.urlshortener.cli.command.RootCommand;

public class Main {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand())
                .execute(args);

        System.exit(exitCode);
    }
}
