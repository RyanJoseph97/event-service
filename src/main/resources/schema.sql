-- Drop in reverse FK dependency order
DROP TABLE IF EXISTS comment_likes;
DROP TABLE IF EXISTS saved_events;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS event_rsvps;
DROP TABLE IF EXISTS event_likes;
DROP TABLE IF EXISTS events;

CREATE TABLE events (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      TEXT          NOT NULL,
    location         VARCHAR(255)  NOT NULL,
    start_time       TIMESTAMP     NOT NULL,
    end_time         TIMESTAMP,
    capacity         INT,
    creator_username VARCHAR(255)  NOT NULL,
    created_at       TIMESTAMP     NOT NULL,
    visibility       VARCHAR(20)   NOT NULL DEFAULT 'PUBLIC',
    image_url        VARCHAR(2048)
);

CREATE INDEX idx_events_creator ON events(creator_username);
CREATE INDEX idx_events_start   ON events(start_time);

CREATE TABLE event_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id   BIGINT       NOT NULL,
    username   VARCHAR(255) NOT NULL,
    liked_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_like_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uq_like       UNIQUE (event_id, username)
);

CREATE TABLE event_rsvps (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id   BIGINT       NOT NULL,
    username   VARCHAR(255) NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_rsvp_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uq_rsvp       UNIQUE (event_id, username)
);

CREATE TABLE comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id   BIGINT       NOT NULL,
    username   VARCHAR(255) NOT NULL,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_comment_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE TABLE comment_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT       NOT NULL,
    username   VARCHAR(255) NOT NULL,
    liked_at   TIMESTAMP    NOT NULL,
    CONSTRAINT fk_comment_like FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    CONSTRAINT uq_comment_like UNIQUE (comment_id, username)
);

CREATE TABLE saved_events (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT       NOT NULL,
    username VARCHAR(255) NOT NULL,
    saved_at TIMESTAMP    NOT NULL,
    CONSTRAINT fk_saved_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uq_saved_event UNIQUE (event_id, username)
);

-- Seed events with future dates so they appear in feed queries (startAfter=now)
INSERT INTO events (title, description, location, start_time, end_time, capacity, creator_username, created_at, visibility) VALUES
    ('Chill at Park',    'Come by to just chill at the park',         'Zilker Park, Austin',    '2027-06-01 14:00:00', '2027-06-01 18:00:00', NULL, 'jdoe',   NOW(), 'PUBLIC'),
    ('Code & Coffee',    'Join us to code and drink coffee together', 'Mozart''s Coffee, Austin','2027-06-02 10:00:00', '2027-06-02 13:00:00', 20,   'asmith', NOW(), 'PUBLIC'),
    ('Live Music Night', 'Local bands playing all evening',           '6th Street, Austin',     '2027-06-03 20:00:00', '2027-06-03 23:59:00', 100,  'bwayne', NOW(), 'PUBLIC');
