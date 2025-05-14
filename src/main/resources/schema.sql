DROP TABLE IF EXISTS events;

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    start_time DATE,
    end_time DATE,
    creator_id BIGINT
);

INSERT INTO events (title, description, location, start_time, end_time, creator_id) VALUES
('Chill at Park', 'Come by to just chill at the park', 'Zilker Park, Austin', '2025-06-01', '2025-06-01', 1),
('Code & Coffee', 'Join us to code and drink coffee together', 'Mozart’s Coffee, Austin', '2025-06-02', '2025-06-02', 2),
('Live Music Night', 'Local bands playing all evening', '6th Street, Austin', '2025-06-03', '2025-06-03', 3);
