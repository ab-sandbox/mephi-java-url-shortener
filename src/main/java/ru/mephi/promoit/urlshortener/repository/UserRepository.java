package ru.mephi.promoit.urlshortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mephi.promoit.urlshortener.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
