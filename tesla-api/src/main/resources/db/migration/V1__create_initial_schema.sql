CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(80) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY ,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    full_name VARCHAR(120) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE container_types(
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE containers (
    id BIGSERIAL PRIMARY KEY ,
    container_type_id BIGINT NOT NULL REFERENCES container_types(id),
    code VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE machines(
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(80) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shifts (
    id BIGSERIAL PRIMARY KEY ,
    name VARCHAR(90) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE receptions (
    id BIGSERIAL PRIMARY KEY ,
    container_id BIGINT NOT NULL REFERENCES containers(id),
    profile_id BIGINT NOT NULL REFERENCES profiles(id),
    operator_id BIGINT NOT NULL REFERENCES users(id),
    lot VARCHAR(80) NOT NULL,
    received_quantity INTEGER NOT NULL CHECK (received_quantity > 0),
    status VARCHAR(40) NOT NULL,
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE inventory_items (
id BIGSERIAL PRIMARY KEY,
reception_id BIGINT NOT NULL UNIQUE REFERENCES receptions(id),
available_quantity INTEGER NOT NULL CHECK (available_quantity >= 0),
status VARCHAR(40) NOT NULL,
version BIGINT NOT NULL DEFAULT 0,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE cutting_records (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id),
    machine_Id BIGINT NOT NULL REFERENCES machines(id),
    operator_id BIGINT NOT NULL REFERENCES users(id),
    shift_id BIGINT NOT NULL REFERENCES shifts(id),
    initial_quantity INTEGER NOT NULL CHECK (initial_quantity > 0),
    good_quantity INTEGER NOT NULL CHECK(good_quantity >= 0),
    scrap_quantity INTEGER NOT NULL CHECK (scrap_quantity >= 0),
    cut_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT cutting_quantity_rule check (initial_quantity = good_quantity + scrap_quantity)
);

CREATE TABLE scrap_records (
    id BIGSERIAL PRIMARY KEY ,
    cutting_record_id BIGINT NOT NULL REFERENCES cutting_records(id),
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE molding_outputs (
    id BIGSERIAL PRIMARY KEY ,
    cutting_record_id BIGINT NOT NULL REFERENCES cutting_records(id),
    quantity_sent INTEGER NOT NULL CHECK(quantity_sent >= 0),
    operator_id BIGINT NOT NULL REFERENCES users(id),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);



