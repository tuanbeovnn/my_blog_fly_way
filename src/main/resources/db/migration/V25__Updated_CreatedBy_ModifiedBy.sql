-- Updated table posts
ALTER TABLE blog_dbo2.posts
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table comments
ALTER TABLE blog_dbo2.comments
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table roles
ALTER TABLE blog_dbo2.roles
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table categories
ALTER TABLE blog_dbo2.categories
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table user_verification_token
ALTER TABLE blog_dbo2.user_verification_token
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table favorites
ALTER TABLE blog_dbo2.favorites
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table refresh_token
ALTER TABLE blog_dbo2.refresh_token
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table user_device
ALTER TABLE blog_dbo2.user_device
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table users_firebase_device
ALTER TABLE blog_dbo2.users_firebase_device
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table followers
ALTER TABLE blog_dbo2.followers
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);
-- Updated table messages_failed
ALTER TABLE blog_dbo2.messages_failed
ALTER
COLUMN created_by TYPE varchar(50),
ALTER
COLUMN modified_by TYPE varchar(50);

