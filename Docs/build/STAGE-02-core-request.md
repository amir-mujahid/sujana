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
- [x] `:shared`: `RequestType`, `RequestStatus` enums; `CreateRequestRequest`, `RequestDto`.
- [x] Backend Flyway `V3__requests.sql`: `requests(id, type, requester_id fk users, status,
      pickup_lat, pickup_lng, pickup_address, dropoff_school_id null, notes, photo_url null,
      created_at, updated_at)`.
- [x] Backend `feature/request`: `POST /requests`, `GET /requests` (role/owner-scoped),
      `GET /requests/{id}`, `POST /requests/{id}/cancel`. Enforce ownership/role.
- [x] App data: `RequestRepository` + Retrofit service + DTO↔domain mappers.
- [x] App domain: `CreatePickupRequest`, `GetMyRequests`, `GetRequestDetail`, `CancelRequest`.
- [x] Maps location picker composable (maps-compose): drop/drag marker, current-location
      button (runtime location permission), reverse-geocode to an address string.
- [x] Optional photo: capture/pick → upload to **Cloudinary** (OkHttp REST, not SDK) → store returned `photo_url`.
- [x] Compose: `CreateRequestScreen`, `MyRequestsScreen` (list), `RequestDetailScreen`
      (status chip + timeline), with empty/loading/error states.
- [x] Wire into Contributor home navigation.

## Data-model changes (Postgres)
- `V3__schools_and_requests.sql` — minimal `schools` table (id, name, lat, lng, tenant_id null)
  seeded with 5 Selangor schools, plus `requests` table as spec'd. Both enriched in Stage 7.

## API endpoints
- `POST /requests` · `GET /requests` · `GET /requests/{id}` · `POST /requests/{id}/cancel`.
- Bonus: `GET /schools` (needed by the app to populate the dropoff school dropdown).

## Acceptance criteria
- [x] Contributor creates a request with a map-picked location; it persists in Postgres at
      `PENDING` and appears in "My requests".
- [x] Request detail shows location, notes, photo (if any), and status.
- [x] List/detail are scoped: a user sees only their own requests; backend rejects access to
      others' requests.
- [x] Cancel transitions an open request to `CANCELLED`.

## Handoff notes (fill when done)
- **Schools source decided here**: minimal `schools` table + 5 seed rows in `V3__schools_and_requests.sql`.
  `GET /schools` returns the list. Enriched with full admin CRUD in Stage 7.
- **Cloudinary path convention for photos**: `sujana/requests/{tempId}/{filename}` where
  `tempId = "temp_${System.currentTimeMillis()}"` (requestId not available until after POST).
  In a future cleanup pass, use the real requestId after creation if needed.
- **Cloudinary config**: user must add to `local.properties`:
  `cloudinary.cloud_name=<your_cloud_name>` and `cloudinary.upload_preset=<your_preset>`.
  Defaults are `"your_cloud_name"` / `"sujana_unsigned"` which will cause upload failures —
  this is expected until the user sets up their Cloudinary account.
- **Cloudinary SDK NOT used** — `cloudinary-android` library brings Fresco with 16 KB page
  alignment issues on AGP 9.2.1. Using OkHttp REST (`api.cloudinary.com/v1_1/…/image/upload`)
  via a `@Named("cloudinary")` OkHttp client (no auth interceptor). See `CloudinaryUploader.kt`.
- **Geocoding approach**: Android `Geocoder` (`getFromLocation` deprecated path, suppressed)
  run on `Dispatchers.IO`. Falls back to `"$lat, $lng"` if Geocoder not present or fails.
- **Role scoping**: CONTRIBUTOR/SCHOOL_ADMIN/SCHOOL_STAFF see only own requests; MPS roles see all.
  Rider role also marked as `canViewAll` for Stage 3 assignment work.

## Resume / progress
- **Status**: Stage complete ✅

## Status
✅ Done.
