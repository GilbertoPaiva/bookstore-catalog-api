-- ──────────────────────────────────────────────────────────────────────────────
-- V1 — Migra IDs de BIGSERIAL para UUID (UUIDv4 via gen_random_uuid)
-- ──────────────────────────────────────────────────────────────────────────────

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ──────────────────────────────────────────────────────────────────────────────
-- 1. CATEGORIES
-- ──────────────────────────────────────────────────────────────────────────────

ALTER TABLE categories ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();

UPDATE categories SET uuid_id = gen_random_uuid() WHERE uuid_id IS NULL;

ALTER TABLE categories ALTER COLUMN uuid_id SET NOT NULL;

-- ──────────────────────────────────────────────────────────────────────────────
-- 2. BOOKS — adiciona coluna temporária antes de tocar em category_id
-- ──────────────────────────────────────────────────────────────────────────────

ALTER TABLE books ADD COLUMN uuid_id UUID DEFAULT gen_random_uuid();

UPDATE books SET uuid_id = gen_random_uuid() WHERE uuid_id IS NULL;

ALTER TABLE books ALTER COLUMN uuid_id SET NOT NULL;

ALTER TABLE books ADD COLUMN category_uuid UUID;

UPDATE books b
SET category_uuid = c.uuid_id
FROM categories c
WHERE b.category_id = c.id;

ALTER TABLE books ALTER COLUMN category_uuid SET NOT NULL;

-- ──────────────────────────────────────────────────────────────────────────────
-- 3. CATEGORIES — troca a PK
-- ──────────────────────────────────────────────────────────────────────────────

ALTER TABLE categories DROP CONSTRAINT categories_pkey;

ALTER TABLE categories DROP COLUMN id;

ALTER TABLE categories RENAME COLUMN uuid_id TO id;

ALTER TABLE categories ADD PRIMARY KEY (id);

-- ──────────────────────────────────────────────────────────────────────────────
-- 4. BOOKS — troca a PK e a FK de categoria
-- ──────────────────────────────────────────────────────────────────────────────

ALTER TABLE books DROP CONSTRAINT IF EXISTS fk_books_category;
ALTER TABLE books DROP CONSTRAINT IF EXISTS books_category_id_fkey;

ALTER TABLE books DROP CONSTRAINT books_pkey;

ALTER TABLE books DROP COLUMN id;
ALTER TABLE books DROP COLUMN category_id;

ALTER TABLE books RENAME COLUMN uuid_id TO id;
ALTER TABLE books RENAME COLUMN category_uuid TO category_id;

ALTER TABLE books ADD PRIMARY KEY (id);

ALTER TABLE books
    ADD CONSTRAINT fk_books_category
    FOREIGN KEY (category_id) REFERENCES categories(id);

