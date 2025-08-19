ALTER TABLE blog_dbo2.favorites
    ADD COLUMN IF NOT EXISTS comment_id UUID;

ALTER TABLE blog_dbo2.favorites
    ADD FOREIGN KEY (comment_id) REFERENCES comments (id);

ALTER TABLE favorites
    ADD COLUMN IF NOT EXISTS object_type TEXT NOT NULL DEFAULT 'POST';

ALTER TABLE blog_dbo2.comments
    ADD COLUMN IF NOT EXISTS likes BIGINT;

UPDATE blog_dbo2.comments
SET likes = 0
where likes is null;

ALTER TABLE blog_dbo2.comments
    ALTER COLUMN likes SET DEFAULT 0,
ALTER
COLUMN likes SET NOT NULL;