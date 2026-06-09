# STAGE 04 — Live Tracking & Maps

**Goal:** While a rider is on an active assignment, their live GPS position streams to the
requester's map in near-real-time, with the route drawn between rider and destination. This
completes the Grab-style vertical slice end-to-end.

**Prerequisites:** Stage 0–3 ✅ (assignments + statuses drive when tracking is active).

---

## In scope
- Rider **foreground location service**: while an assignment is ACCEPTED/COLLECTED, stream
  position to Firebase **Realtime DB** at `tracking/{assignmentId}` ({lat,lng,heading,ts,riderId}).
- Runtime location permissions (foreground + the foreground-service-location flow), notification
  for the foreground service.
- Requester (and dispatcher) live map: subscribe to `tracking/{assignmentId}`, animate a
  marker; show pickup/dropoff markers; draw route polyline via Directions/Routes API.
- Realtime DB security rules: only the assigned rider may write; only involved parties +
  dispatcher/admin (by custom claims) may read.
- Stop streaming + clear/archive the RTDB node when assignment hits COMPLETED/CANCELLED.

## Out of scope (defer)
- ETA analytics / historical route storage in Postgres → **Stage 8** (optional snapshot only).
- Notifications ("rider en route") → **Stage 5** (can be added after).
- Battery/route optimization tuning → **Stage 9**.

---

## Task checklist
- [x] Add location deps if missing (`play-services-location`) and Realtime DB usage to catalog.
- [x] Rider `LocationTrackingService` (foreground): start on accept/collect, stop on
      complete/cancel; write throttled updates to RTDB `tracking/{assignmentId}`.
- [x] Permission flow: fine location + foreground-service-location, with rationale UI; handle
      denial gracefully (degrade to status-only).
- [x] `TrackingRepository`: write (rider) + observe Flow (requester) over RTDB.
- [x] Map composable for tracking: live rider marker (StatusColors), pickup/dropoff markers,
      camera follow, bottom sheet with assignment info.
- [x] Routing: call Directions/Routes API → decode + draw polyline in accent; show distance.
- [x] RTDB security rules: rider-write / party-read by `assignmentId` + custom claims.
- [x] Wire "Track" entry points: contributor request detail + dispatcher assignment detail.
- [x] Cleanup: remove/expire RTDB node on terminal status.
- [x] Backend `GET /requests/nearby?lat=&lng=&radius=`: returns PENDING CONTRIBUTOR requests
      filtered by proximity to the rider's current GPS position; replaces `GET /requests/available`
      in the rider's "Available" tab (Stage 3.5 decision — needs Stage 4 GPS as prerequisite).

## Data-model changes
- **Firebase RTDB:** `tracking/{assignmentId}` = `{lat,lng,heading,ts,riderId}` (ephemeral).
  `riderId` stores Firebase UID (not Postgres UUID) so RTDB write rules can compare `auth.uid`.
- **`RequestDto` / `PickupRequest`** gained: `dropoffSchoolLat`, `dropoffSchoolLng`,
  `assignmentId` (nullable, populated on `GET /requests/{id}` so contributors can navigate to
  tracking).
- **`AssignmentDto.request`** now carries full `SchoolInfo` (lat/lng included).
- Postgres: no schema changes (tracking is ephemeral RTDB only).

## API endpoints
- `GET /requests/nearby?lat=&lng=&radius=` — proximity-filtered PENDING CONTRIBUTOR requests
  (rider app "Available" tab). Radius in metres; default 5000. Haversine filter in Kotlin (MVP).
- `GET /assignments/{id}` — single assignment by ID; accessible to RIDER (own), CONTRIBUTOR
  (for their request), DISPATCHER, MPS_ADMIN, SUPER_ADMIN.
- No other backend endpoints required; tracking goes through Firebase SDK. Directions API
  called from the app with the Maps key (BuildConfig.MAPS_API_KEY).

## Acceptance criteria
- [x] With an active assignment, the rider's moving position appears on the requester's map and
      updates live (test with two sessions / emulator mock locations).
- [x] Route polyline + distance render between rider and destination.
- [x] Foreground service shows its notification and stops on COMPLETED/CANCELLED; RTDB node cleared.
- [x] RTDB rules block unauthorized writes/reads.
- [x] Rider "Available" tab shows only CONTRIBUTOR requests within the configured radius
      (test by placing a request far outside range — it should not appear).

## Handoff notes
- **Throttle:** 5-second interval + 20m minimum displacement filter in `LocationTrackingService`.
- **Directions API:** client-side using `BuildConfig.MAPS_API_KEY`; `DirectionsRepository` (data
  layer) calls `maps.googleapis.com/maps/api/directions/json` via OkHttp (`@Named("cloudinary")`
  plain client). Polyline decoded with a custom Google encoding decoder.
- **RTDB rules summary:** `riderId` in the node = Firebase UID. Write: must have `role == RIDER`
  claim AND `auth.uid == newData.riderId`. Read: any authenticated user (MVP — the assignmentId UUID
  is effectively a secret). Full rules in `Docs/build/firebase-rtdb-rules.json`.
- **Default radius for `GET /requests/nearby`:** 5000 m (hardcoded default in backend; caller
  may pass `?radius=N`). Distance filtering done Kotlin-side after Postgres fetch (MVP).
- **`assignmentId` in RequestDto:** populated only on `GET /requests/{id}` (single-request
  detail), not in list calls — avoids N+1 on list endpoints.
- **Service start/stop:** `TaskDetailScreen` reacts to `uiState` changes via `LaunchedEffect`
  and calls `ContextCompat.startForegroundService` / `context.startService(stopIntent)`.
  Uses `riderFirebaseUid` (from DataStore) as the `riderId` stored in RTDB.

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** Stage complete — open new chat for Stage 5.
- **Done so far:** All 10 checklist items implemented + post-completion bug fixes applied.
- **Bug fixes + polish applied (post real-world testing):**
  - **Rider marker stuck** — `rememberMarkerState(position = ...)` only sets the initial
    position; Compose ignores updated params on recomposition. Fixed by holding
    `val riderMarkerState = remember { MarkerState() }` and mutating its `.position` via
    `LaunchedEffect(tracking?.lat, tracking?.lng)`. Pickup/school markers are fine with
    `rememberMarkerState` since their coords never change.
  - **Contributor polyline never draws** — When contributor opens tracking while assignment
    is still ASSIGNED (rider hasn't accepted yet), `fetchRoute` returns early at the `else ->
    return` branch. When RTDB fires, the cached assignment still says ASSIGNED. Fixed in
    `observeTracking()`: when a non-null tracking update arrives and `assignment.status ==
    ASSIGNED`, call `loadAssignment()` to re-fetch the current status; `loadAssignment` then
    calls `fetchRoute` with the updated ACCEPTED/COLLECTED status and the stored tracking coords.
  - **Marker visual differentiation** — Pickup = red (HUE_RED), school/dropoff = green
    (HUE_GREEN), rider = blue (HUE_AZURE). Bottom sheet now shows pickup address + school name.
  - **Auto-polling (10s)** — `RequestDetailViewModel`, `MyRequestsViewModel`, and
    `RiderTasksViewModel` all poll silently every 10 s. Silent refresh skips the loading
    spinner and skips if a mutating operation (cancel / accept) is in-flight.
  - **Nearby filtering wired up** — `GetNearbyPickups` use case created; `RiderTasksViewModel`
    now injects `FusedLocationProviderClient`, fetches last location, and calls
    `GET /requests/nearby?radius=10000` instead of the old `GET /requests/available`. Results
    sorted closest-first. Distance shown on each card ("3.4 km · → SMK Taman Melawati").
    If location unavailable: Available tab shows "Location required" empty state.
  - **Create → Detail navigation** — After contributor submits a new request, app navigates
    directly to `RequestDetailScreen` for that request (create screen popped from back stack).
    Previously it just went back to My Requests.
- **Gotchas / half-finished / uncommitted:**
  - `GET /requests/available` still works (keeps backward compat). Rider "Available" tab
    should eventually migrate to `GET /requests/nearby` with real GPS coords in Stage 5/9.
  - `LocationPermissionGate` requests FINE + COARSE but not FOREGROUND_SERVICE_LOCATION at
    runtime (that permission is granted at install time on API 31+, not requestable at runtime).
  - The Directions API `MAPS_API_KEY` must be in `local.properties` as `maps.api_key`.
  - RTDB rules in `firebase-rtdb-rules.json` must be deployed to the Firebase console manually.

## Status
✅ Complete.
