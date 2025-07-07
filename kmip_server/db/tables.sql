CREATE TABLE managed_objects (
    id UUID PRIMARY KEY,
    object_type VARCHAR(50) NOT NULL, -- Symmetric Key, Private Key, etc.
    state VARCHAR(20) NOT NULL, -- Pre-Active, Active, Deactivated, Compromised, Destroyed
    kms_key_id VARCHAR(255) NOT NULL, -- Reference to external KMS key
    creation_date TIMESTAMP NOT NULL, -- Set when object is initially created
    activation_date TIMESTAMP, -- Set when object becomes valid for use, can be future date
    process_start_date TIMESTAMP, -- Set when object can begin being used for cryptographic operations
    protect_stop_date TIMESTAMP, -- Set when object should stop being used for protecting new data
    deactivation_date TIMESTAMP, -- Set when Deactivate operation is called or protect_stop_date reached
    destruction_date TIMESTAMP, -- Set when Destroy operation is executed successfully
    last_modified_date TIMESTAMP NOT NULL, -- Auto-updated on any attribute modification
    compromise_occurrence_date TIMESTAMP, -- Set when Revoke operation is called with compromise reason
    deleted BOOLEAN DEFAULT FALSE,
    CONSTRAINT valid_state CHECK (state IN ('PRE_ACTIVE', 'ACTIVE', 'DEACTIVATED', 'COMPROMISED', 'DESTROYED'))
);

CREATE TABLE object_attributes (
    id UUID PRIMARY KEY,
    object_id UUID NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT NOT NULL,
    attribute_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    FOREIGN KEY (object_id) REFERENCES managed_objects(id),
    CONSTRAINT unique_attribute UNIQUE (object_id, attribute_name)
);

CREATE TABLE object_attributes (
    id UUID PRIMARY KEY,
    object_id UUID NOT NULL,
    attribute_name VARCHAR(100) NOT NULL,
    attribute_value TEXT NOT NULL,
    attribute_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    FOREIGN KEY (object_id) REFERENCES managed_objects(id),
    CONSTRAINT unique_attribute UNIQUE (object_id, attribute_name)
);

CREATE TABLE access_policies (
    id UUID PRIMARY KEY,
    client_identity VARCHAR(255) NOT NULL,
    operation_type VARCHAR(50) NOT NULL, -- Create, Get, Destroy, etc.
    object_type VARCHAR(50), -- Optional restriction by object type
    permission BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL,
    CONSTRAINT unique_policy UNIQUE (client_identity, operation_type, object_type)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    client_identity VARCHAR(255) NOT NULL,
    operation_type VARCHAR(50) NOT NULL,
    object_id UUID,
    status VARCHAR(20) NOT NULL,
    request_details JSONB,
    response_details JSONB,
    error_message TEXT,
    FOREIGN KEY (object_id) REFERENCES managed_objects(id)
);

CREATE TABLE client_credentials (
    id UUID PRIMARY KEY,
    client_identity VARCHAR(255) UNIQUE NOT NULL,
    auth_type VARCHAR(50) NOT NULL, -- Certificate, Username/Password, etc.
    auth_data JSONB NOT NULL, -- Stored securely (hashed/encrypted as needed)
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    modified_at TIMESTAMP NOT NULL
);

-- For Split/Join/DeriveKey, you might add:
CREATE TABLE object_links (
    id UUID PRIMARY KEY,
    source_object_id UUID NOT NULL REFERENCES managed_objects(id),
    target_object_id UUID NOT NULL REFERENCES managed_objects(id),
    link_type VARCHAR(50) NOT NULL, -- e.g., 'split', 'join', 'derived', 'rekey
    created_at TIMESTAMP NOT NULL
);

-- For object groups:
CREATE TABLE object_groups (
    id UUID PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE object_group_membership (
    group_id UUID NOT NULL REFERENCES object_groups(id),
    object_id UUID NOT NULL REFERENCES managed_objects(id),
    PRIMARY KEY (group_id, object_id)
);