CREATE TABLE blog_dbo2.viewers
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id       UUID NOT NULL,
    view_ct       INTEGER NOT NULL DEFAULT 0
);