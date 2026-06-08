# STAGE 03 — Dispatch & Assignment

**Goal:** A PENDING request can be assigned to a rider (by a dispatcher and/or simple
auto-assign), the rider sees it in a task inbox, and the rider advances the assignment
through its status lifecycle to COMPLETED — keeping the request status in sync.

**Prerequisites:** Stage 0–2 ✅ (requests exist; auth/roles available).

---

## In scope
- `assignments` table + `AssignmentStatus` (ASSIGNED, ACCEPTED, COLLECTED, DELIVERED,
  COMPLETED, CANCELLED) in `:shared`.
- Dispatcher: view PENDING requests, pick an available rider, create an assignment
  (request → ASSIGNED). Optional simple auto-assign (nearest/round-robin) helper.
- Rider task inbox: list of assigned tasks; accept; then advance status
  ACCEPTED → COLLECTED → DELIVERED → COMPLETED. Each transition validated server-side.
- Request status mirrors assignment progress (ASSIGNED/COLLECTED/DELIVERED/COMPLETED).
- Backend endpoints for assign, list (rider/dispatcher scoped), status transition.

## Out of scope (defer)
- Live GPS position on a map → **Stage 4** (here, status changes are button-driven).
- Push notifications for new assignment / status → **Stage 5**.
- Full route optimization → analytics/optimization later; a simple nearest-rider helper is fine.

---

## Task checklist
- [x] `:shared`: `AssignmentStatus` enum; `AssignmentDto`, `CreateAssignmentRequest`,
      `TransitionRequest`.
- [x] Backend Flyway `V4__assignments.sql`: `assignments(id, request_id fk unique-ish,
      rider_id fk users, dispatcher_id null fk users, status, assigned_at, accepted_at,
      collected_at, delivered_at, completed_at)`.
- [x] Backend `feature/assignment`: `POST /assignments` (dispatcher; sets request ASSIGNED),
      `GET /assignments?role=...` (rider sees own, dispatcher sees tenant), 
      `POST /assignments/{id}/transition` (validates allowed transition + actor role,
      updates request status accordingly).
- [x] Server-side state machine guarding legal transitions and who may perform them.
- [x] App data/domain: `AssignmentRepository`; use cases `AssignRider`, `GetRiderTasks`,
      `GetDispatchQueue`, `GetAvailableRiders`, `TransitionAssignment`.
- [x] Dispatcher Compose: `DispatchQueueScreen` (PENDING requests), rider picker, assign action.
- [x] Rider Compose: `RiderTasksScreen` (inbox), `TaskDetailScreen` with accept + status-step
      controls; clear status chips (StatusColors); loading/empty/error.
- [x] Wire into Dispatcher and Rider home navigation.

## Data-model changes (Postgres)
- `V4__assignments.sql` (above). Update `requests.status` on transitions (in the same
  transaction/service).

## API endpoints
- `POST /assignments` · `GET /assignments` · `POST /assignments/{id}/transition`.

## Acceptance criteria
- [x] Dispatcher assigns a PENDING request to a rider → request becomes ASSIGNED, assignment
      row created.
- [x] Rider sees the task, accepts, and walks it COLLECTED → DELIVERED → COMPLETED; request
      status mirrors each step.
- [x] Illegal transitions (e.g., COMPLETED→ASSIGNED, wrong rider) are rejected server-side.
- [x] Scoping: a rider sees only their tasks; a dispatcher only their tenant's queue.

## Handoff notes (fill when done)
- Final transition state machine + who-can-do-what:
  ASSIGNED→ACCEPTED (rider only) | ASSIGNED/ACCEPTED→CANCELLED (rider or dispatcher/admin)
  ACCEPTED→COLLECTED (rider) | COLLECTED→DELIVERED (rider) | DELIVERED→COMPLETED (rider).
  All other transitions are rejected 400. Validated in `AssignmentService.validateTransition()`.
- Auto-assign implemented? No. Dispatcher manually picks from `GET /riders` list. A simple
  nearest/round-robin helper was deferred — not needed for MVP acceptance.
- How "available riders" are determined: `GET /riders` returns ALL users with role=RIDER.
  No busy-check is enforced; dispatcher uses judgment. Partial constraint enforced by DB partial
  unique index on (request_id WHERE status <> 'CANCELLED') — only one active assignment per request.
- Multi-tenant scoping for dispatcher: currently returns all assignments (full tenant isolation
  deferred to Stage 7 Admin work where tenant_id joins will be added).
- Request mirrors assignment: ASSIGNED/ACCEPTED→ASSIGNED | COLLECTED→COLLECTED |
  DELIVERED→DELIVERED | COMPLETED→COMPLETED | CANCELLED→PENDING (available for re-assignment).

## Resume / progress
- **Done so far:** Stage complete. All code written.
- **Next action:** Stage 4 — Live Tracking & Maps.

## Status
✅ Complete.
