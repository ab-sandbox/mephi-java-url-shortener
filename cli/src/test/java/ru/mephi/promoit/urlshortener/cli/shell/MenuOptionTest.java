package ru.mephi.promoit.urlshortener.cli.shell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MenuOptionTest {

    @Test
    void shouldFindOptionByCode() {
        assertEquals(
                MenuOption.CREATE,
                MenuOption.fromCode("1").orElseThrow()
        );

        assertEquals(
                MenuOption.EXIT,
                MenuOption.fromCode("0").orElseThrow()
        );
    }

    @Test
    void shouldReturnEmptyForUnknownCode() {
        assertTrue(MenuOption.fromCode("99").isEmpty());
    }
}
