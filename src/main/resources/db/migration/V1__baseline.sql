-- Baseline schema for event-service (PostgreSQL).
-- Generated from the JPA entity model via Hibernate DDL export, then hand-formatted
-- and given readable constraint names. This is the V1 Flyway baseline; all future
-- schema changes go in new V2+ migrations. Hibernate runs in `validate` mode against
-- this schema in the docker/prod profile, so column names and types must stay in sync
-- with the entities.

create table events (
    id                   bigserial      not null,
    title                varchar(255)   not null,
    description          TEXT           not null,
    location             varchar(255)   not null,
    latitude             float8,
    longitude            float8,
    start_time           timestamp      not null,
    end_time             timestamp,
    capacity             int4,
    creator_username     varchar(255)   not null,
    visibility           varchar(255)   not null,
    category             varchar(255)   not null,
    image_url            varchar(2048),
    recurrence_type      varchar(255)   not null,
    recurrence_end_date  date,
    created_at           timestamp      not null,
    primary key (id)
);
create index idx_event_creator_username on events (creator_username);
create index idx_event_start_time       on events (start_time);
create index idx_event_visibility       on events (visibility);
create index idx_event_category         on events (category);

create table comments (
    id          bigserial      not null,
    event_id    int8           not null,
    username    varchar(255)   not null,
    content     TEXT           not null,
    created_at  timestamp      not null,
    primary key (id)
);

create table comment_likes (
    id          bigserial      not null,
    comment_id  int8           not null,
    username    varchar(255)   not null,
    liked_at    timestamp      not null,
    primary key (id),
    constraint uk_comment_likes_comment_username unique (comment_id, username),
    constraint fk_comment_likes_comment foreign key (comment_id) references comments
);

create table event_likes (
    id        bigserial      not null,
    event_id  int8           not null,
    username  varchar(255)   not null,
    liked_at  timestamp      not null,
    primary key (id),
    constraint uk_event_likes_event_username unique (event_id, username),
    constraint fk_event_likes_event foreign key (event_id) references events
);

create table event_rsvps (
    id          bigserial      not null,
    event_id    int8           not null,
    username    varchar(255)   not null,
    status      varchar(255)   not null,
    created_at  timestamp      not null,
    updated_at  timestamp,
    primary key (id),
    constraint uk_event_rsvps_event_username unique (event_id, username),
    constraint fk_event_rsvps_event foreign key (event_id) references events
);
create index idx_event_rsvp_username on event_rsvps (username);

create table event_invites (
    id                bigserial      not null,
    event_id          int8           not null,
    invitee_username  varchar(255)   not null,
    invited_at        timestamp      not null,
    primary key (id),
    constraint uk_event_invites_event_invitee unique (event_id, invitee_username),
    constraint fk_event_invites_event foreign key (event_id) references events
);

create table saved_events (
    id        bigserial      not null,
    event_id  int8           not null,
    username  varchar(255)   not null,
    saved_at  timestamp      not null,
    primary key (id),
    constraint uk_saved_events_event_username unique (event_id, username),
    constraint fk_saved_events_event foreign key (event_id) references events
);
create index idx_saved_event_username on saved_events (username);
