package ru.mephi.promoit.urlshortener.cli.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class UserSession {

    private static final String USER_ID_KEY = "user.id";

    private final Path sessionFile;

    public UserSession() {
        this(Path.of(
                System.getProperty("user.home"),
                ".url-shortener",
                "session.properties"
        ));
    }

    public UserSession(Path sessionFile) {
        this.sessionFile = sessionFile;
    }

    public Optional<UUID> getUserId() throws IOException {
        if (Files.notExists(sessionFile)) {
            return Optional.empty();
        }

        Properties properties = new Properties();

        try (InputStream input = Files.newInputStream(sessionFile)) {
            properties.load(input);
        }

        String value = properties.getProperty(USER_ID_KEY);

        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid user ID in session file", e);
        }
    }

    public void saveUserId(UUID userId) throws IOException {
        Path directory = sessionFile.toAbsolutePath().getParent();
        Files.createDirectories(directory);

        Properties properties = new Properties();
        properties.setProperty(USER_ID_KEY, userId.toString());

        try (OutputStream output = Files.newOutputStream(sessionFile)) {
            properties.store(output, "URL Shortener CLI session");
        }
    }
}
