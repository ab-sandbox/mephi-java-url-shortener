package ru.mephi.promoit.urlshortener.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void shouldGenerateCodeWithRequestedLength() {
        String code = generator.generate(8);

        assertEquals(8, code.length());
        assertTrue(code.matches("[0-9a-zA-Z]{8}"));
    }

    @Test
    void shouldRejectNonPositiveLength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(-1)
        );
    }
}
