package ru.mephi.promoit.urlshortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mephi.promoit.urlshortener.model.Notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO notifications (
                user_id,
                short_code,
                type,
                message,
                created_at
            )
            VALUES (
                :userId,
                :shortCode,
                :type,
                :message,
                :createdAt
            )
            ON CONFLICT (short_code, type) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("userId") UUID userId,
            @Param("shortCode") String shortCode,
            @Param("type") String type,
            @Param("message") String message,
            @Param("createdAt") Instant createdAt
    );

    Optional<Notification> findByIdAndUser_Id(Long id, UUID userId);
}
