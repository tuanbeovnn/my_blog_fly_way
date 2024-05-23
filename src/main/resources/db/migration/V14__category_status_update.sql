-- Step 1: Alter the table to add the status column
ALTER TABLE blog_dbo2.categories
    ADD COLUMN status BOOLEAN;

-- Step 2: Update the status column to true for all existing rows
UPDATE blog_dbo2.categories
SET status = TRUE;
