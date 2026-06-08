# STAGE 04 — Live Tracking & Maps

**Goal:** While a rider is on an active assignment, their live GPS position streams to the
requester's map in near-real-time, with the route drawn between rider and destination. This
completes the Grab-style vertical slice end-to-end.

**Prerequisites:** Stage 0–3 ✅ (assignments + statuses drive when tracking is active).

---

## In scope
- Rider **foreground location service**: while an assignment is ACCEPTED/COLLECTED, stream
  position to Firebase **Realtime DB** at `tracking/{assignmentId}` ({lat,lng,heading,ts}).
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
- [ ] Add location deps if missing (`play-services-location`) and Realtime DB usage to catalog.
- [ ] Rider `LocationTrackingService` (foreground): start on accept/collect, stop on
      complete/cancel; write throttled updates to RTDB `tracking/{assignmentId}`.
- [ ] Permission flow: fine location + foreground-service-location, with rationale UI; handle
      denial gracefully (degrade to status-only).
- [ ] `TrackingRepository`: write (rider) + observe Flow (requester) over RTDB.
- [ ] Map composable for tracking: live rider marker (StatusColors), pickup/dropoff markers,
      camera follow, bottom sheet with assignment info.
- [ ] Routing: call Directions/Routes API → decode + draw polyline in accent; show distance.
- [ ] RTDB security rules: rider-write / party-read by `assignmentId` + custom claims.
- [ ] Wire "Track" entry points: contributor request detail + dispatcher assignment detail.
- [ ] Cleanup: remove/expire RTDB node on terminal status.

## Data-model changes
- **Firebase RTDB:** `tracking/{assignmentId}` = `{lat,lng,heading,ts,riderId}` (ephemeral).
- Postgres: optionally `assignments.last_lat/last_lng/last_ping_at` snapshot for a static
  fallback (optional).

## API endpoints
- None required (tracking goes through Firebase SDK). Directions API called from the app with
  the Maps key (or proxied via backend if you prefer to hide the key — note decision).

## Acceptance criteria
- [ ] With an active assignment, the rider's moving position appears on the requester's map and
      updates live (test with two sessions / emulator mock locations).
- [ ] Route polyline + distance render between rider and destination.
- [ ] Foreground service shows its notification and stops on COMPLETED/CANCELLED; RTDB node cleared.
- [ ] RTDB rules block unauthorized writes/reads.

## Handoff notes (fill when done)
- Update throttle interval/distance filter chosen: _____
- Directions API called client-side or proxied via backend: _____
- RTDB rules summary: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
