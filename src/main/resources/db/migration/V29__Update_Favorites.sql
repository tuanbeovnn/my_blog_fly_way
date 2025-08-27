ALTER TABLE blog_dbo2.favorites
    ADD COLUMN IF NOT EXISTS comment_id UUID;

ALTER TABLE blog_dbo2.favorites
    ADD FOREIGN KEY (comment_id) REFERENCES comments (id);

ALTER TABLE blog_dbo2.favorites
    ADD COLUMN IF NOT EXISTS object_type TEXT NOT NULL DEFAULT 'POST';

UPDATE blog_dbo2.favorites
  SET object_type = 'POST'
WHERE object_type IS NULL;

ALTER TABLE blog_dbo2.comments
    ADD COLUMN IF NOT EXISTS likes BIGINT;


