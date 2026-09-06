CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    visa_type VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    residence_region VARCHAR(255) NOT NULL
);
