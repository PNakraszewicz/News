CREATE TABLE IF NOT EXISTS source (
    id SERIAL PRIMARY KEY,
    source_id VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    url TEXT,
    category VARCHAR(255),
    language VARCHAR(10),
    country VARCHAR(10)
);