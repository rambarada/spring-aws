CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    original_file_name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    processed_at TIMESTAMP,
    file_size BIGINT,
    content_type VARCHAR(255),
    error_message VARCHAR(255)
);