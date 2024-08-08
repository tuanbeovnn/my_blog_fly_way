CREATE TABLE messages_failed
(
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_token   VARCHAR(255),
    message        TEXT,
    event_type     VARCHAR(50),
    object_details TEXT,
    status         BOOLEAN,
    created_date   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    modified_date  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(20),
    modified_by    VARCHAR(20)
);
