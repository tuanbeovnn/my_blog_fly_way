ALTER TABLE favorites ALTER COLUMN post_id DROP NOT NULL;
ALTER TABLE favorites ALTER COLUMN comment_id DROP NOT NULL;

DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'favorites_user_post_unique') THEN
ALTER TABLE favorites DROP CONSTRAINT favorites_user_post_unique;
END IF;
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'favorites_user_comment_unique') THEN
ALTER TABLE favorites DROP CONSTRAINT favorites_user_comment_unique;
END IF;
END $$;

-- 2) Clean bad historical rows before adding the CHECK:
--    a) rows with neither post_id nor comment_id
DELETE FROM favorites WHERE post_id IS NULL AND comment_id IS NULL;

--    b) rows that (incorrectly) have both set – keep post_id and drop comment_id (or choose your policy)
UPDATE favorites
SET comment_id = NULL
WHERE post_id IS NOT NULL
  AND comment_id IS NOT NULL;

-- 3) Enforce XOR: exactly one target is set
ALTER TABLE favorites
    ADD CONSTRAINT favorites_exactly_one_target CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL)
            OR
        (post_id IS NULL AND comment_id IS NOT NULL)
        );

-- 4) (Optional but recommended) enforce objectType matches the target
-- Comment in if you want DB-level safety for objectType too:
-- ALTER TABLE favorites
--   ADD CONSTRAINT favorites_object_type_matches CHECK (
--     (object_type = 'POST'    AND post_id IS NOT NULL AND comment_id IS NULL)
--     OR
--     (object_type = 'COMMENT' AND comment_id IS NOT NULL AND post_id IS NULL)
--   );

-- 5) Prevent duplicates (one favorite per user per target)
CREATE UNIQUE INDEX IF NOT EXISTS ux_fav_user_post
    ON favorites(user_id, post_id)
    WHERE post_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_fav_user_comment
    ON favorites(user_id, comment_id)
    WHERE comment_id IS NOT NULL;

-- 6) (Nice to have) ensure referential cleanup if a post/comment is deleted
-- Drop and re-add FKs with ON DELETE CASCADE (adjust constraint names & parent tables)
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_favorites_post') THEN
ALTER TABLE favorites DROP CONSTRAINT fk_favorites_post;
END IF;
  IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_favorites_comment') THEN
ALTER TABLE favorites DROP CONSTRAINT fk_favorites_comment;
END IF;
END $$;

ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE;

ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_comment
        FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE;
