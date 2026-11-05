package ru.mephi.promoit.urlshortener.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunModeTest {

    @Test
    void shouldSelectInteractiveModeWithoutArguments() {
        assertEquals(
                RunMode.INTERACTIVE,
                RunMode.fromArgs(new String[0])
        );
    }

    @Test
    void shouldSelectCommandModeWithArguments() {
        assertEquals(
                RunMode.COMMAND,
                RunMode.fromArgs(new String[]{"list"})
        );
    }
}
