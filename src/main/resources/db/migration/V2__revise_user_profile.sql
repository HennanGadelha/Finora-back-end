ALTER TABLE users
    DROP COLUMN currency,
    DROP COLUMN timezone,
    ADD COLUMN birth_date DATE NOT NULL;