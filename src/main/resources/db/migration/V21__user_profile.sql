-- Migration script to create the profiles table

CREATE TABLE blog_dbo2.profiles
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    bio        TEXT,
    website    TEXT,
    location   TEXT,
    avatar_url TEXT,
    user_id    UUID NOT NULL REFERENCES blog_dbo2.users (id) ON DELETE CASCADE,
    twitter    TEXT,
    linkedin   TEXT,
    github     TEXT
);

