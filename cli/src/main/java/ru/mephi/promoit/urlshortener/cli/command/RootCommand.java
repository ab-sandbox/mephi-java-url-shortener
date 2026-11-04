package ru.mephi.promoit.urlshortener.cli.command;

import picocli.CommandLine.Command;

@Command(
        name = "url-shortener",
        description = "URL Shortener command-line client",
        mixinStandardHelpOptions = true
)
public class RootCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use --help to see available commands.");
    }
}
