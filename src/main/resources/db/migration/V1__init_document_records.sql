CREATE TABLE document_records (
    id UUID PRIMARY KEY,
    recipient_id VARCHAR(255) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_hash VARCHAR(256) NOT NULL UNIQUE,
    storage_path VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP
);