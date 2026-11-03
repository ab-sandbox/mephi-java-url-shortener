package ru.mephi.promoit.urlshortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.mephi.promoit.urlshortener.config.AppProperties;
import ru.mephi.promoit.urlshortener.config.LinkProperties;
import ru.mephi.promoit.urlshortener.dto.CreateLinkRequest;
import ru.mephi.promoit.urlshortener.dto.LinkResponse;
import ru.mephi.promoit.urlshortener.exception.LinkUnavailableException;
import ru.mephi.promoit.urlshortener.model.ShortLink;
import ru.mephi.promoit.urlshortener.model.User;
import ru.mephi.promoit.urlshortener.repository.ShortLinkRepository;
import ru.mephi.promoit.urlshortener.repository.UserRepository;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ShortLinkServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ShortLinkRepository shortLinkRepository;
    @Mock
    private ShortCodeGenerator codeGenerator;

    private ShortLinkService shortLinkService;

    private static final Instant NOW = Instant.parse("2026-11-03T12:00:00Z");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AppProperties appProperties =
                new AppProperties(URI.create("http://localhost:8080"));

        LinkProperties linkProperties =
                new LinkProperties(Duration.ofHours(24), 8);

        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        shortLinkService = new ShortLinkService(
                userRepository,
                shortLinkRepository,
                codeGenerator,
                linkProperties,
                appProperties,
                clock
        );
    }

    @Test
    void shouldCreateLinkForNewUser() {
        CreateLinkRequest request =
                new CreateLinkRequest("https://example.com", 10);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(codeGenerator.generate(8))
                .thenReturn("aB7xK2qP");

        when(shortLinkRepository.existsByCode("aB7xK2qP"))
                .thenReturn(false);

        when(shortLinkRepository.save(any(ShortLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LinkResponse response =
                shortLinkService.createLink(request, null);

        assertNotNull(response.userId());
        assertEquals("aB7xK2qP", response.code());
        assertEquals(
                "http://localhost:8080/aB7xK2qP",
                response.shortUrl()
        );
        assertEquals(10, response.clickLimit());
        assertEquals(0, response.clickCount());
        assertEquals(NOW.plus(Duration.ofHours(24)), response.expiresAt());

        verify(userRepository).save(any(User.class));
        verify(shortLinkRepository).save(any(ShortLink.class));
    }

    @Test
    void shouldCreateLinkForExistingUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, NOW);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(codeGenerator.generate(8))
                .thenReturn("test1234");

        when(shortLinkRepository.existsByCode("test1234"))
                .thenReturn(false);

        when(shortLinkRepository.save(any(ShortLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateLinkRequest request =
                new CreateLinkRequest("https://example.com", 5);

        LinkResponse response =
                shortLinkService.createLink(request, userId);

        assertEquals(userId, response.userId());

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldRejectInvalidUrl() {
        CreateLinkRequest request =
                new CreateLinkRequest("file:///etc/passwd", 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> shortLinkService.createLink(request, null)
        );

        verifyNoInteractions(userRepository, shortLinkRepository);
    }


    @Test
    void shouldResolveAvailableLink() {
        User user = new User(UUID.randomUUID(), NOW);

        ShortLink link = new ShortLink(
                user,
                "test1234",
                "https://example.com",
                2,
                NOW.minusSeconds(60),
                NOW.plusSeconds(3600)
        );

        when(shortLinkRepository.findByCode("test1234"))
                .thenReturn(Optional.of(link));

        when(shortLinkRepository.incrementClickCount("test1234", NOW))
                .thenReturn(1);

        URI result = shortLinkService.resolveLink("test1234");

        assertEquals(URI.create("https://example.com"), result);

        verify(shortLinkRepository)
                .incrementClickCount("test1234", NOW);
    }

    @Test
    void shouldRejectExpiredLink() {
        User user = new User(UUID.randomUUID(), NOW);

        ShortLink link = new ShortLink(
                user,
                "expired1",
                "https://example.com",
                10,
                NOW.minusSeconds(7200),
                NOW.minusSeconds(3600)
        );

        when(shortLinkRepository.findByCode("expired1"))
                .thenReturn(Optional.of(link));

        assertThrows(
                LinkUnavailableException.class,
                () -> shortLinkService.resolveLink("expired1")
        );

        verify(shortLinkRepository, never())
                .incrementClickCount(anyString(), any(Instant.class));
    }

    @Test
    void shouldRejectLinkWhenAtomicUpdateFails() {
        User user = new User(UUID.randomUUID(), NOW);

        ShortLink link = new ShortLink(
                user,
                "test1234",
                "https://example.com",
                2,
                NOW.minusSeconds(60),
                NOW.plusSeconds(3600)
        );

        when(shortLinkRepository.findByCode("test1234"))
                .thenReturn(Optional.of(link));

        when(shortLinkRepository.incrementClickCount("test1234", NOW))
                .thenReturn(0);

        assertThrows(
                LinkUnavailableException.class,
                () -> shortLinkService.resolveLink("test1234")
        );
    }
}
