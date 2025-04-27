CREATE TABLE roles (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

INSERT INTO roles (id, name) VALUES
    (1, 'administrator'),
    (2, 'renter'),
    (3, 'manager'),
    (4, 'user');

CREATE TABLE rooms (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    status INTEGER CHECK(status IN (0, 1, 2)) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role_id INTEGER NOT NULL,
    room_id INTEGER,
    card_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

INSERT INTO users (username, password_hash, role_id, room_id, created_at, updated_at) VALUES
    ('admin', '$2a$10$Hr1E99611gJD7EDihlC5nuSOB.dHMfk0ueKsMraSsAT/WXtBjImBa', 1, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

CREATE INDEX idx_users_room_id ON users(room_id);
CREATE INDEX idx_users_role_id ON users(role_id);