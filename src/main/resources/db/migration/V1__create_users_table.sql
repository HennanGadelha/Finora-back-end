CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'BRL',
    timezone VARCHAR(64) NOT NULL DEFAULT 'America/Sao_Paulo',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_at TIMESTAMPTZ,
    CONSTRAINT users_email_unique UNIQUE (email),
    CONSTRAINT users_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT users_status_deactivation_consistent CHECK (
        (status = 'ACTIVE' AND deactivated_at IS NULL)
        OR (status = 'INACTIVE' AND deactivated_at IS NOT NULL)
    )
);

CREATE FUNCTION set_users_updated_at()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;

CREATE TRIGGER users_updated_at_trigger
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_users_updated_at();