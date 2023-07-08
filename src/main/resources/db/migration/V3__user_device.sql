CREATE TABLE blog_dbo.user_device (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id           UUID NOT NULL REFERENCES users(id),
    device_type       VARCHAR(255),
    device_id         VARCHAR(255) NOT NULL,
    is_refresh_active boolean DEFAULT false,
    created_date       TIMESTAMP,
    modified_date      TIMESTAMP,
    created_by         VARCHAR(20),
    modified_by        VARCHAR(20)
);