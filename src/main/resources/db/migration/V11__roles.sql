-- Create the roles table
CREATE TABLE blog_dbo2.roles
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_date       TIMESTAMP,
    modified_date      TIMESTAMP,
    created_by         VARCHAR(20),
    modified_by        VARCHAR(20)
);

-- Create the user_roles join table
CREATE TABLE blog_dbo2.user_roles
(
    user_id UUID NOT NULL REFERENCES blog_dbo2.users(id),
    role_id UUID NOT NULL REFERENCES blog_dbo2.roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Insert ROLE_ADMIN record into roles table
INSERT INTO blog_dbo2.roles (id, name, created_date, modified_date, created_by, modified_by)
VALUES (uuid_generate_v4(), 'ROLE_ADMIN', NOW(), NOW(), 'system', 'system');

-- Insert ROLE_THERAPIST record into roles table
INSERT INTO blog_dbo2.roles (id, name, created_date, modified_date, created_by, modified_by)
VALUES (uuid_generate_v4(), 'ROLE_THERAPIST', NOW(), NOW(), 'system', 'system');

-- Insert ROLE_USER record into roles table
INSERT INTO blog_dbo2.roles (id, name, created_date, modified_date, created_by, modified_by)
VALUES (uuid_generate_v4(), 'ROLE_USER', NOW(), NOW(), 'system', 'system');

-- Insert ROLE_USER for existing users
INSERT INTO blog_dbo2.user_roles (user_id, role_id)
SELECT u.id, r.id
FROM blog_dbo2.users u
         CROSS JOIN blog_dbo2.roles r
WHERE r.name = 'ROLE_USER';