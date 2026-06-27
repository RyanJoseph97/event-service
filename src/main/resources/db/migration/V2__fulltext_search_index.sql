-- PostgreSQL full-text search index for the events.fullTextSearch() native query
-- (matched with `@@ plainto_tsquery('english', ...)`), enabled via app.search.fts.enabled.
-- This is an expression-based GIN index over title + description; it has no JPA entity
-- mapping, so it lives in a migration rather than the entity-derived V1 baseline.
-- IF NOT EXISTS guards against pre-Flyway dev databases that already created it via the
-- old schema-docker.sql bootstrap.
CREATE INDEX IF NOT EXISTS idx_events_fts
    ON events USING GIN (to_tsvector('english', coalesce(title, '') || ' ' || coalesce(description, '')));
