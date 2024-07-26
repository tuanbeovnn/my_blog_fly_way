ALTER TABLE blog_dbo2.favorites
    ADD CONSTRAINT user_post_unique UNIQUE (user_id, post_id);

-- Ensure the slug is unique
ALTER TABLE blog_dbo2.categories
    ADD CONSTRAINT unique_slug UNIQUE (slug);

-- Insert 'Java' if it does not exist
INSERT INTO blog_dbo2.categories (name, created_date, modified_date, created_by, modified_by, status, slug)
SELECT 'Java', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', true, 'java'
    WHERE NOT EXISTS (SELECT 1 FROM blog_dbo2.categories WHERE slug = 'java');

-- Insert 'JavaScript' if it does not exist
INSERT INTO blog_dbo2.categories (name, created_date, modified_date, created_by, modified_by, status, slug)
SELECT 'JavaScript', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', true, 'javascript'
    WHERE NOT EXISTS (SELECT 1 FROM blog_dbo2.categories WHERE slug = 'javascript');

-- Insert 'Devops' if it does not exist
INSERT INTO blog_dbo2.categories (name, created_date, modified_date, created_by, modified_by, status, slug)
SELECT 'Devops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', true, 'devops'
    WHERE NOT EXISTS (SELECT 1 FROM blog_dbo2.categories WHERE slug = 'devops');
