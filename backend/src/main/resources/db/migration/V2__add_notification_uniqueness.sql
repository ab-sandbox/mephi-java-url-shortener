ALTER TABLE notifications
    ADD CONSTRAINT uk_notifications_short_code_type
        UNIQUE (short_code, type);