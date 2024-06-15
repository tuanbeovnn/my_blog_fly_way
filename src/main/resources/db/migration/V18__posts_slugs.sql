-- Add the new column
ALTER TABLE blog_dbo2.posts
    ADD COLUMN slug TEXT NULL;

-- Update existing rows with generated slugs
UPDATE blog_dbo2.posts
SET slug = LOWER(REPLACE(title, ' ', '-'))
WHERE title IS NOT NULL;