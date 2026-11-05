package ru.mephi.promoit.urlshortener.cli.shell;

import java.util.Arrays;
import java.util.Optional;

public enum MenuOption {

    CREATE("1", "Create link"),
    LIST("2", "List links"),
    OPEN("3", "Open link"),
    UPDATE("4", "Update link"),
    DELETE("5", "Delete link"),
    NOTIFICATIONS("6", "Notifications"),
    EXIT("0", "Exit");

    private final String code;
    private final String description;

    MenuOption(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<MenuOption> fromCode(String code) {
        return Arrays.stream(values())
                .filter(option -> option.code.equals(code))
                .findFirst();
    }
}
