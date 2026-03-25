CREATE DATABASE market;
\c market

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS users(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login varchar(256) UNIQUE
);

CREATE TABLE IF NOT EXISTS items(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title varchar(256),
    description TEXT,
    price DECIMAL,
    count INT
);
CREATE INDEX IF NOT EXISTS items_title_trgm_idx ON items USING GIN (to_tsvector('russian'::regconfig, title));
CREATE INDEX IF NOT EXISTS items_description_trgm_idx ON items USING GIN (to_tsvector('russian'::regconfig, description));

CREATE TABLE IF NOT EXISTS carts(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) UNIQUE
);
CREATE INDEX IF NOT EXISTS carts_user_idx ON carts(user_id);

CREATE TABLE IF NOT EXISTS cart_items(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id),
    cart_id BIGINT REFERENCES carts(id),
    count INT,
    CONSTRAINT uq_item_cart UNIQUE (item_id, cart_id)
);

CREATE TABLE IF NOT EXISTS orders(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS orders_user_idx ON orders(user_id);

CREATE TABLE IF NOT EXISTS order_items(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id),
    order_id BIGINT REFERENCES orders(id),
    title varchar(256),
    price DECIMAL,
    count INT
);
CREATE INDEX IF NOT EXISTS order_items_order_idx ON order_items(order_id);

CREATE TABLE IF NOT EXISTS images(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    item_id BIGINT REFERENCES items(id) UNIQUE,
    file_name TEXT,
    content BYTEA
);
CREATE INDEX IF NOT EXISTS images_item_idx ON images(item_id);