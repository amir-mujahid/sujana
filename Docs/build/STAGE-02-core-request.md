# STAGE 02 — Core Request System

**Goal:** A Contributor can create a waste pickup request (with a map-picked location), and
see their requests in a list and detail view. The request lifecycle starts at `PENDING`.
This establishes the central `requests` entity the whole logistics flow revolves around.

**Prerequisites:** Stage 0 ✅, Stage 1 ✅ (auth + roles available; calls are authenticated).

---

## In scope
- `requests` table + `RequestType` (CONTRIBUTOR | SCHOOL) and `RequestStatus`
  (PENDING, ASSIGNED, COLLECTED, DELIVERED, COMPLETED, CANCELLED) enums in `:shared`.
- Backend CRUD-ish endpoints: create, list (scoped to requester/role), get by id, cancel.
- Contributor "Create request" screen: pickup location via a **Google Maps location picker**
  (place marker / use current location), notes, optional photo (**Cloudinary upload**), dropoff
  school selector (from minimal schools data).
- Contributor "My requests" list + request detail screen with status timeline.
- Both request *types* modeled in the schema; this stage's UI focuses on CONTRIBUTOR
  (SCHOOL submission UI comes in Stage 6 but the type/column exist now).

## Out of scope (defer)
- Assigning a rider / dispatch → **Stage 3**.
- Live tracking on the map → **Stage 4** (here the map is only a location picker).
- School-side submission flow → **Stage 6**.
- Notifications on status change → **Stage 5**.

---

## Task checklist
- [ ] `:shared`: `RequestType`, `RequestStatus` enums; `CreateRequestRequest`, `RequestDto`.
- [ ] Backend Flyway `V3__requests.sql`: `requests(id, type, requester_id fk users, status,
      pickup_lat, pickup_lng, pickup_address, dropoff_school_id null, notes, photo_url null,
      created_at, updated_at)`.
- [ ] Backend `feature/request`: `POST /requests`, `GET /requests` (role/owner-scoped),
      `GET /requests/{id}`, `POST /requests/{id}/cancel`. Enforce ownership/role.
- [ ] App data: `RequestRepository` + Retrofit service + DTO↔domain mappers.
- [ ] App domain: `CreatePickupRequest`, `GetMyRequests`, `GetRequestDetail`, `CancelRequest`.
- [ ] Maps location picker composable (maps-compose): drop/drag marker, current-location
      button (runtime location permission), reverse-geocode to an address string.
- [ ] Optional photo: capture/pick → upload to **Cloudinary** (signed upload preset) → store returned `photo_url`.
- [ ] Compose: `CreateRequestScreen`, `MyRequestsScreen` (list), `RequestDetailScreen`
      (status chip + timeline), with empty/loading/error states.
- [ ] Wire into Contributor home navigation.

## Data-model changes (Postgres)
- `V3__requests.sql` — `requests` table as above. `dropoff_school_id` references a schools
  table; if schools aren't created until Stage 7, add a minimal `schools` table here (id,
  name, lat, lng, tenant_id null) and seed a few, then enrich in Stage 7.

## API endpoints
- `POST /requests` · `GET /requests` · `GET /requests/{id}` · `POST /requests/{id}/cancel`.

## Acceptance criteria
- [ ] Contributor creates a request with a map-picked location; it persists in Postgres at
      `PENDING` and appears in "My requests".
- [ ] Request detail shows location, notes, photo (if any), and status.
- [ ] List/detail are scoped: a user sees only their own requests; backend rejects access to
      others' requests.
- [ ] Cancel transitions an open request to `CANCELLED`.

## Handoff notes (fill when done)
- Schools source decided here (minimal table + seeds vs full): _____
- Cloudinary path convention for photos: _____
- Geocoding approach used for address string: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
