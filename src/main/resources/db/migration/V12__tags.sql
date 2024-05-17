-- Create the tags table
CREATE TABLE blog_dbo2.tags
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_date       TIMESTAMP,
    modified_date      TIMESTAMP,
    created_by         VARCHAR(20),
    modified_by        VARCHAR(20)
);

-- Create the post_tags join table
CREATE TABLE blog_dbo2.post_tags
(
    post_id UUID NOT NULL REFERENCES blog_dbo2.posts(id),
    tag_id UUID NOT NULL REFERENCES blog_dbo2.tags(id),
    PRIMARY KEY (post_id, tag_id)

);