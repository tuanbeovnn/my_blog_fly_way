CREATE TABLE blog_dbo2.categories
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_date       TIMESTAMP,
    modified_date      TIMESTAMP,
    created_by         VARCHAR(20),
    modified_by        VARCHAR(20)
);