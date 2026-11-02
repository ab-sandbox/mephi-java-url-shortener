package ru.mephi.promoit.urlshortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.promoit.urlshortener.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_IdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);
}
