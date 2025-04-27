CREATE EXTENSION IF NOT EXISTS plpython3u;

-- Create table for biometric data
CREATE TABLE IF NOT EXISTS biometric_data (
    id SERIAL PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paw_data JSONB
);

INSERT INTO biometric_data (ip_address, paw_data) 
VALUES ('127.0.0.1', '{"paw_id": "KBV-2024-001", "species": "Hydrochoerus hydrochaeris", "measurements": {"length": 12.5, "width": 8.3, "pad_count": 4, "claw_length": 2.1}, "notes": "Healthy adult specimen from Kapibarovsk Central Park"}'); 
