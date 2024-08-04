CREATE DATABASE IF NOT EXISTS destroyer;

USE destroyer;

CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Insert a sample user
INSERT INTO user (username, password) VALUES ('admin', '<REDACTED>');