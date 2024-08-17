
ALTER TABLE blog_dbo2.users
    ADD COLUMN user_name VARCHAR(255);

UPDATE blog_dbo2.users u
SET user_name = CONCAT(
        SPLIT_PART(u.email, '@', 1),
        '_',
    LEFT(REPLACE(uuid_generate_v4()::text, '-', ''), 8)
)
WHERE u.user_name IS NULL;

