ALTER TABLE blog_dbo2.favorites
    ADD COLUMN IF NOT EXISTS comment_id UUID;

ALTER TABLE blog_dbo2.favorites
    ADD FOREIGN KEY (comment_id) REFERENCES comments (id);

ALTER TABLE favorites
    ADD COLUMN IF NOT EXISTS object_type TEXT NOT NULL DEFAULT 'POST';

ALTER TABLE blog_dbo2.comments
    ADD COLUMN IF NOT EXISTS likes BIGINT;


