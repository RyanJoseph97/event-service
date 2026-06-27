# event-service

Manages events, RSVPs, likes, comments, saved events, and invites for EventMaster.

Port: `8081`. Context path: `/event-service`.

## Visibility

Events have two visibility levels:

- `PUBLIC` — visible to all users. Creating a PUBLIC event requires the creator's `AccountStatus` to be `VERIFIED` or `TRUSTED`. Verified via a call to user-service before the event is saved.
- `INVITE_ONLY` — visible only to the creator and explicitly invited users. Any authenticated user can create an INVITE_ONLY event.

## Endpoints

### Events
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `GET` | `/events` | Optional | List/search events (paginated). Supports `keyword`, `location`, `creatorUsername`, `startAfter`, `startBefore`, `visibility`, `category` query params. Unauthenticated callers see only PUBLIC events. |
| `GET` | `/events/{id}` | Optional | Get event by ID. Returns 403 if INVITE_ONLY and caller lacks invite. |
| `GET` | `/events/by-creator/{username}` | Optional | Events by a given creator |
| `POST` | `/events` | Required | Create an event |
| `PATCH` | `/events/{id}` | Required | Update an event (creator only) |
| `DELETE` | `/events/{id}` | Required | Delete an event (creator only) |

### RSVPs
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/events/{id}/rsvp` | Required | RSVP with status `GOING` or `INTERESTED` |
| `DELETE` | `/events/{id}/rsvp` | Required | Remove RSVP |
| `GET` | `/events/{id}/rsvps` | Required | List all RSVPs for an event |
| `GET` | `/events/{id}/rsvps/me` | Required | Get caller's RSVP status |

### Likes
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/events/{id}/like` | Required | Like an event |
| `DELETE` | `/events/{id}/like` | Required | Unlike an event |
| `GET` | `/events/{id}/likes` | Required | Like count |
| `GET` | `/events/{id}/likes/me` | Required | Whether the caller has liked the event |

### Comments
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/events/{eventId}/comments` | Required | Post a comment |
| `GET` | `/events/{eventId}/comments` | Required | List comments |
| `POST` | `/comments/{commentId}/like` | Required | Like a comment |
| `DELETE` | `/comments/{commentId}/like` | Required | Unlike a comment |

### Saved Events
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `POST` | `/events/{eventId}/save` | Required | Save an event |
| `DELETE` | `/events/{eventId}/save` | Required | Unsave an event |
| `GET` | `/users/{username}/saved-events` | Required | List saved events (routed from api-gateway) |
| `GET` | `/users/{username}/rsvped-events` | Required | List RSVP'd events (routed from api-gateway) |

### Invites
| Method | Path | Auth | Notes |
|--------|------|------|-------|
| `GET` | `/events/{id}/invites` | Required | List invites (creator only) |
| `POST` | `/events/{id}/invites` | Required | Send invite (creator only) |
| `DELETE` | `/events/{id}/invites/{username}` | Required | Revoke invite (creator only) |

## Cross-Service Communication

- **user-service**: Called before creating a PUBLIC event to verify the creator's `AccountStatus`. On event update, if start time or location changes, notifications are sent to all GOING/INTERESTED attendees via user-service's internal notification endpoint. Both calls degrade gracefully on failure.

## Running Locally

```bash
cd event-service
mvn spring-boot:run
```

Uses H2 in-memory database. Available at `http://localhost:8081/event-service`.

## Environment Variables

| Variable | Required | Default | Notes |
|----------|----------|---------|-------|
| `JWT_SECRET` | No | `eventmaster-shared-dev-secret-key-change-in-prod` | Must match all services |
| `USER_SERVICE_BASE_URL` | No | `http://localhost:8080` | Used to verify AccountStatus and send notifications |

## Testing

```bash
mvn test
mvn test -Dtest=EventServiceTest
```
