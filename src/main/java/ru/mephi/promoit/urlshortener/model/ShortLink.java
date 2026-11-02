
package ru.mephi.promoit.urlshortener.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "short_links")
public class ShortLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "original_url", nullable = false, columnDefinition = "text")
    private String originalUrl;

    @Column(name = "click_limit", nullable = false)
    private int clickLimit;

    @Column(name = "click_count", nullable = false)
    private int clickCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ShortLink() {
    }

    public ShortLink(
            User user,
            String code,
            String originalUrl,
            int clickLimit,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.user = user;
        this.code = code;
        this.originalUrl = originalUrl;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getClickCount() {
        return clickCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void updateClickLimit(int clickLimit) {
        if (clickLimit < clickCount || clickLimit <= 0) {
            throw new IllegalArgumentException(
                    "Click limit must be positive and not less than click count"
            );
        }

        this.clickLimit = clickLimit;
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isLimitReached() {
        return clickCount >= clickLimit;
    }
}
