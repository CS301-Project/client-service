CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "citext";

CREATE TYPE gender_types AS ENUM ('MALE', 'FEMALE', 'NON-BINARY', 'PREFER NOT TO SAY');
CREATE TYPE client_status_types AS ENUM ('PENDING', 'ACTIVE', 'INACTIVE');
CREATE TABLE client_profile (
    client_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(50) NOT NULL
    CHECK (
        char_length(first_name) BETWEEN 2 AND 50
        AND first_name ~ '^[A-Za-z ]+$'
    ),
    last_name VARCHAR(50) NOT NULL
    CHECK (
        char_length(last_name) BETWEEN 2 AND 50
        AND last_name ~ '^[A-Za-z ]+$'
    ),
   date_of_birth DATE NOT NULL
   CHECK (
        date_of_birth <= CURRENT_DATE
        AND date_of_birth >= (CURRENT_DATE - INTERVAL '100 years')
        AND date_of_birth <= (CURRENT_DATE - INTERVAL '18 years')
    ),
    gender gender_types NOT NULL,
    email_address CITEXT NOT NULL
    CHECK (
        email_address ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'
    ),
    phone_number VARCHAR(16) NOT NULL UNIQUE
    CHECK (
        phone_number ~ '^\+[0-9]{10,15}$'
    ),
    address VARCHAR(100) NOT NULL
    CHECK (
        char_length(address) BETWEEN 5 AND 100
    ),
    city VARCHAR(50) NOT NULL
    CHECK (
        char_length(city) BETWEEN 2 AND 50
    ),
    state VARCHAR(50) NOT NULL
    CHECK (
        char_length(state) BETWEEN 2 AND 50
    ),
    country VARCHAR(50) NOT NULL
    CHECK (
        char_length(country) BETWEEN 2 AND 5
    ),
    postal_code VARCHAR(10) NOT NULL
    CHECK (
        char_length(postal_code) BETWEEN 4 AND 10
    ),
    status client_status_types NOT NULL
);

INSERT INTO client_profile (
  client_id, first_name, last_name, date_of_birth, gender,
  email_address, phone_number, address, city, state, country, postal_code, status
) VALUES
('a1b2c3d4-e5f6-4788-990a-b1c2d3e4f5a6', 'Alice', 'Johnson', '1990-05-15', 'FEMALE',
 'alice.johnson@email.com', '+12025550101', '123 Main St', 'New York', 'NY', 'USA', '10001', 'ACTIVE'),
('b2c3d4e5-f6a7-4899-801b-c2d3e4f5a6b7', 'Bob', 'Smith', '1985-12-03', 'MALE',
 'bob.smith@email.com', '+13105550102', '456 Oak Ave', 'Los Angeles', 'CA', 'USA', '90210', 'ACTIVE'),
('c3d4e5f6-a7b8-4900-912c-d3e4f5a6b7c8', 'Carol', 'Davis', '1992-08-20', 'FEMALE',
 'carol.davis@email.com', '+12123330103', '789 Pine Rd', 'Chicago', 'IL', 'USA', '60601', 'ACTIVE'),
('d4e5f6a7-b8c9-4011-023d-e4f5a6b7c8d9', 'David', 'Wilson', '1988-11-12', 'MALE',
 'david.wilson@email.com', '+13055550104', '321 Elm Street', 'Miami', 'FL', 'USA', '33101', 'ACTIVE'),
('e5f6a7b8-c9d0-4122-134e-f5a6b7c8d9e0', 'Emma', 'Brown', '1995-07-08', 'FEMALE',
 'emma.brown@email.com', '+14165550105', '654 Maple Drive', 'Toronto', 'ON', 'CA', 'M5V 3A8', 'ACTIVE');

