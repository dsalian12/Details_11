CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(64)    NOT NULL UNIQUE,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(2000),
    price       NUMERIC(12, 2) NOT NULL,
    quantity    INTEGER        NOT NULL DEFAULT 0,
    version     BIGINT,
    created_at  TIMESTAMPTZ,
    updated_at  TIMESTAMPTZ
);

CREATE INDEX idx_products_name ON products (name);
