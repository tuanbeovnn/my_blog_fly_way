-- Add the new column
ALTER TABLE blog_dbo2.categories
    ADD COLUMN slug TEXT NULL UNIQUE;

DO $$
DECLARE
rec RECORD;
    base_slug TEXT;
    counter INT;
    uuid_part TEXT;
BEGIN
    -- Update slugs for each category
FOR rec IN
SELECT id, name
FROM blog_dbo2.categories
         LOOP
     -- Generate base slug based on name
    base_slug := LOWER(REPLACE(rec.name, ' ', '-'));

-- Generate a UUID part for uniqueness using uuid_generate_v4()
SELECT uuid_generate_v4()::text INTO uuid_part;

-- Initialize counter for uniqueness check
counter := 1;

        -- Check if the generated slug already exists
        WHILE EXISTS (
            SELECT 1
            FROM blog_dbo2.categories
            WHERE slug = base_slug || '-' || uuid_part
              AND id <> rec.id
        ) LOOP
            -- Increment counter and update slug
            base_slug := base_slug || '-' || counter;
            counter := counter + 1;
END LOOP;

        -- Update the category with the unique slug
UPDATE blog_dbo2.categories
SET slug = base_slug || '-' || uuid_part
WHERE id = rec.id;
END LOOP;
END $$;
