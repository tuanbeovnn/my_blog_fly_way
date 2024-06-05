CREATE TABLE blog_dbo2.followers
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id UUID NOT NULL,
    followed_user_id UUID NOT NULL,
    type VARCHAR(20),
    created_date  TIMESTAMP,
    modified_date TIMESTAMP,
    created_by    VARCHAR(20),
    modified_by   VARCHAR(20),
    FOREIGN KEY (follower_id) REFERENCES users (id),
    FOREIGN KEY (followed_user_id) REFERENCES users (id)
);

ALTER TABLE blog_dbo2.users
    ADD COLUMN followers BIGINT NOT NULL DEFAULT 0;