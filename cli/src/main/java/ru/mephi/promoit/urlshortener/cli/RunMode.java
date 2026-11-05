package ru.mephi.promoit.urlshortener.cli;

public enum RunMode {

    INTERACTIVE,
    COMMAND;

    public static RunMode fromArgs(String[] args) {
        return args.length == 0
                ? INTERACTIVE
                : COMMAND;
    }
}
