package ru.mephi.promoit.urlshortener.cli.shell;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import ru.mephi.promoit.urlshortener.cli.command.RootCommand;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractiveShellTest {

    @Test
    void shouldExitWhenZeroSelected() {
        String output = runShell("0\n");

        assertTrue(output.contains("=== URL Shortener ==="));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void shouldHandleUnknownOption() {
        String output = runShell("99\n0\n");

        assertTrue(output.contains("Unknown option."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void shouldExecuteListCommand() {
        TestListCommand listCommand = new TestListCommand();

        CommandLine commandLine = createCommandLine(listCommand);

        runShell("2\n0\n", commandLine);

        assertEquals(1, listCommand.executionCount);
    }

    @Test
    void shouldExecuteMultipleCommands() {
        TestListCommand listCommand = new TestListCommand();

        CommandLine commandLine = createCommandLine(listCommand);

        runShell("2\n2\n0\n", commandLine);

        assertEquals(2, listCommand.executionCount);
    }

    @Test
    void shouldExitOnEndOfInput() {
        String output = runShell("");

        assertTrue(output.contains("Goodbye!"));
    }

    private CommandLine createCommandLine(TestListCommand listCommand) {
        return new CommandLine(new RootCommand())
                .addSubcommand(listCommand);
    }

    private String runShell(String input) {
        return runShell(
                input,
                new CommandLine(new RootCommand())
        );
    }

    private String runShell(String input, CommandLine commandLine) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (Scanner scanner = new Scanner(input);
             PrintStream out = new PrintStream(
                     output,
                     true,
                     StandardCharsets.UTF_8
             )) {

            InteractiveShell shell = new InteractiveShell(
                    commandLine,
                    scanner,
                    out
            );

            shell.run();
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    @Command(name = "list")
    static class TestListCommand implements Runnable {

        private int executionCount;

        @Override
        public void run() {
            executionCount++;
        }
    }
}
