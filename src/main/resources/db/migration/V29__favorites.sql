create table if not exists blog_dbo2.favorites(
    id UUID primary key default gen_random_uuid(),
    user_id UUID not null unique ,
    post_id UUID,
    comment_id UUID,
    type varchar(10) not null,
    object_type varchar (10) not null default 'POST',
    created_date  TIMESTAMP,
    modified_date TIMESTAMP,
    created_by    VARCHAR(20),
    modified_by   VARCHAR(20),

    foreign key (user_id) references users(id),
    foreign key (post_id) references posts(id),
    foreign key (comment_id) references comments(id)
);

ALTER TABLE blog_dbo2.comments
    ADD COLUMN IF NOT EXISTS likes BIGINT;

UPDATE blog_dbo2.comments
SET likes = 0 where likes is null;

ALTER TABLE blog_dbo2.comments
    ALTER COLUMN likes SET DEFAULT 0,
ALTER COLUMN likes SET NOT NULL;