-- Insert 'iOS' if it does not exist
INSERT INTO blog_dbo2.categories (name, created_date, modified_date, created_by, modified_by, status, slug)
SELECT 'iOS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', true, 'iOS'
    WHERE NOT EXISTS (SELECT 1 FROM blog_dbo2.categories WHERE slug = 'iOS');
