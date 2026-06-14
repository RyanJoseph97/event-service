-- Tables are created/updated by Hibernate (ddl-auto=update).
-- This file adds indexes idempotently so they exist on both fresh and existing databases.
CREATE INDEX IF NOT EXISTS idx_event_creator_username  ON events(creator_username);
CREATE INDEX IF NOT EXISTS idx_events_fts              ON events USING GIN(to_tsvector('english', coalesce(title,'') || ' ' || coalesce(description,'')));
CREATE INDEX IF NOT EXISTS idx_event_start_time        ON events(start_time);
CREATE INDEX IF NOT EXISTS idx_event_visibility        ON events(visibility);
CREATE INDEX IF NOT EXISTS idx_event_category          ON events(category);
CREATE INDEX IF NOT EXISTS idx_event_rsvp_username     ON event_rsvps(username);
CREATE INDEX IF NOT EXISTS idx_saved_event_username    ON saved_events(username);
