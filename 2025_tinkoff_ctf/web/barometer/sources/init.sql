CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_salt VARCHAR(32) NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS weather_reports (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    temperature DECIMAL(5,2) NOT NULL,
    humidity INTEGER NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    slug VARCHAR(255) UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_weather_reports_user_id ON weather_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_weather_reports_created_at ON weather_reports(created_at);
CREATE INDEX IF NOT EXISTS idx_weather_reports_slug ON weather_reports(slug);

DO $$
DECLARE
    admin_id INTEGER;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin') THEN
        INSERT INTO users (username, password_salt, password_hash)
        VALUES (
            'admin',
            'd7b4c3e2f1a0b9c8d7e6f5a4b3c2d1e0',
            '1577f4d384e3be38e7de99xxxxxxxxxxxxxxxxxxxxxx17be258767f07e5cac5f'
        )
        RETURNING id INTO admin_id;

        INSERT INTO weather_reports (user_id, temperature, humidity, description, slug)
        VALUES (
            admin_id,
            25.5,
            65,
            'Welcome to CapyWeather! This is a sample weather report. Feel free to add your own reports and track the weather conditions in your area.',
            'welcome-to-capyweather-this-is-admin'
        );
    END IF;
END $$; 
