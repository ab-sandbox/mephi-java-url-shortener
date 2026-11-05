package ru.mephi.promoit.urlshortener.cli.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSessionTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnEmptyWhenSessionFileDoesNotExist() throws IOException {
        UserSession session = createSession();

        assertTrue(session.getUserId().isEmpty());
    }

    @Test
    void shouldSaveAndReadUserId() throws IOException {
        UserSession session = createSession();
        UUID userId = UUID.randomUUID();

        session.saveUserId(userId);

        assertEquals(userId, session.getUserId().orElseThrow());
    }

    @Test
    void shouldPersistUserIdBetweenInstances() throws IOException {
        UUID userId = UUID.randomUUID();

        createSession().saveUserId(userId);

        UserSession newSession = createSession();

        assertEquals(userId, newSession.getUserId().orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenUserIdIsMissing() throws IOException {
        Path sessionFile = getSessionFile();

        Files.createDirectories(sessionFile.getParent());
        Files.writeString(sessionFile, "other.property=value");

        assertTrue(createSession().getUserId().isEmpty());
    }

    @Test
    void shouldRejectInvalidUserId() throws IOException {
        Path sessionFile = getSessionFile();

        Files.createDirectories(sessionFile.getParent());
        Files.writeString(sessionFile, "user.id=invalid-uuid");

        assertThrows(IOException.class, () -> createSession().getUserId());
    }

    private UserSession createSession() {
        return new UserSession(getSessionFile());
    }

    private Path getSessionFile() {
        return tempDir.resolve("session/session.properties");
    }
}
