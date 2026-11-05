package ru.mephi.promoit.urlshortener.cli.shell;

import picocli.CommandLine;

import java.util.Scanner;

public class InteractiveShell {

    private final CommandLine commandLine;
    private final Scanner scanner;

    public InteractiveShell(CommandLine commandLine, Scanner scanner) {
        this.commandLine = commandLine;
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            printMenu();

            String input = scanner.nextLine().trim();

            MenuOption option = MenuOption.fromCode(input).orElse(null);

            if (option == null) {
                System.out.println("Unknown option.");
                continue;
            }

            if (option == MenuOption.EXIT) {
                System.out.println("Goodbye!");
                return;
            }

            execute(option);
            System.out.println();
        }
    }

    private void execute(MenuOption option) {
        switch (option) {
            case CREATE -> createLink();
            case LIST -> commandLine.execute("list");
            case OPEN -> openLink();
            case UPDATE -> updateLink();
            case DELETE -> deleteLink();
            case NOTIFICATIONS -> notifications();
            case EXIT -> {
            }
        }
    }

    private void printMenu() {
        System.out.println("=== URL Shortener ===");

        for (MenuOption option : MenuOption.values()) {
            System.out.printf(
                    "%s. %s%n",
                    option.getCode(),
                    option.getDescription()
            );
        }

        System.out.print("Select an option: ");
    }

    private void createLink() {
        String url = prompt("Original URL: ");
        String limit = prompt("Click limit: ");

        commandLine.execute(
                "create",
                "--url", url,
                "--limit", limit
        );
    }

    private void openLink() {
        String code = prompt("Short link code: ");
        commandLine.execute("open", code);
    }

    private void updateLink() {
        String code = prompt("Short link code: ");
        String limit = prompt("New click limit: ");

        commandLine.execute("update", code, "--limit", limit);
    }

    private void deleteLink() {
        String code = prompt("Short link code: ");
        commandLine.execute("delete", code);
    }

    private void notifications() {
        System.out.println("1. All notifications");
        System.out.println("2. Unread notifications");
        System.out.println("3. Mark as read");

        String choice = prompt("Select an option: ");

        switch (choice) {
            case "1" -> commandLine.execute("notifications", "list");
            case "2" -> commandLine.execute("notifications", "unread");
            case "3" -> {
                String id = prompt("Notification ID: ");
                commandLine.execute("notifications", "read", id);
            }
            default -> System.out.println("Unknown option.");
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }
}
