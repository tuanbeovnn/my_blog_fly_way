CREATE TABLE blog_dbo2.posts
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title             TEXT    NOT NULL,
    content           TEXT    NOT NULL,
    status            BOOLEAN          DEFAULT true NOT NULL,
    approved          BOOLEAN NOT NULL DEFAULT false,
    favourite BIGINT NOT NULL DEFAULT 0,
    images            TEXT NULL,
    short_description TEXT NULL,
    thumnails         TEXT NULL,
    category_id       UUID    NOT NULL REFERENCES categories (id),
    user_id           UUID    NOT NULL REFERENCES users (id),
    created_date      TIMESTAMP,
    modified_date     TIMESTAMP,
    created_by        VARCHAR(20),
    modified_by       VARCHAR(20)
);