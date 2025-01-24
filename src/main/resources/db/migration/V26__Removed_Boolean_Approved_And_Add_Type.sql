
ALTER TABLE blog_dbo2.posts
ADD COLUMN post_type VARCHAR(255);

-- Remove the old 'approved' column
ALTER TABLE blog_dbo2.posts
DROP COLUMN approved;


-- Update existing rows with a default value for the new post_type column
UPDATE blog_dbo2.posts
SET post_type = 'PENDING'
WHERE post_type IS NULL;