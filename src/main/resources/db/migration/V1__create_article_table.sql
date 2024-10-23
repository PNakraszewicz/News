CREATE TABLE IF NOT EXISTS article (
                         id SERIAL PRIMARY KEY,
                         source_name VARCHAR(255),
                         author VARCHAR(255),
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         url TEXT,
                         url_to_image TEXT,
                         published_at TIMESTAMP,
                         content TEXT
);
