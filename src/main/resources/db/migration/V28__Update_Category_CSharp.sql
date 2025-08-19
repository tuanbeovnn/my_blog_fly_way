-- Insert 'CSharp' if it does not exist
INSERT INTO blog_dbo2.categories (name, created_date, modified_date, created_by, modified_by, status, slug)
SELECT 'CSharp', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'admin', 'admin', true, 'CSharp'
    WHERE NOT EXISTS (SELECT 1 FROM blog_dbo2.categories WHERE slug = 'CSharp');
