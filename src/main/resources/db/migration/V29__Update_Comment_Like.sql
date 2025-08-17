ALTER TABLE blog_dbo2.comments
ADD COLUMN IF NOT EXISTS likes BIGINT;

UPDATE blog_dbo2.comments
SET likes = 0 where likes is null;

ALTER TABLE blog_dbo2.comments
ALTER COLUMN likes SET DEFAULT 0,
ALTER COLUMN likes SET NOT NULL;