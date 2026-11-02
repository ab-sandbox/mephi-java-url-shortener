package ru.mephi.promoit.urlshortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mephi.promoit.urlshortener.model.ShortLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByCode(String code);

    boolean existsByCode(String code);

    List<ShortLink> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE short_links
            SET click_count = click_count + 1
            WHERE code = :code
              AND click_count < click_limit
              AND expires_at > :now
            """, nativeQuery = true)
    int incrementClickCount(
            @Param("code") String code,
            @Param("now") Instant now
    );

    List<ShortLink> findByExpiresAtLessThanEqual(Instant now);
}
