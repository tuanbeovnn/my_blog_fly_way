

ALTER TABLE blog_dbo2.comments
    ADD COLUMN parent_comment_id UUID REFERENCES blog_dbo2.comments(id);

-- Add a foreign key constraint to ensure referential integrity
ALTER TABLE blog_dbo2.comments
    ADD CONSTRAINT fk_parent_comment
        FOREIGN KEY (parent_comment_id) REFERENCES blog_dbo2.comments(id);