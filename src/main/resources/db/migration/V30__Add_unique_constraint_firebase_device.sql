-- Add unique constraint to prevent duplicate (user_id, device_token) combinations for multi-device support
ALTER TABLE blog_dbo2.users_firebase_device 
ADD CONSTRAINT uk_users_firebase_device_user_device UNIQUE (user_id, device_token);
