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
- [ ] `:shared`: `AssignmentStatus` enum; `AssignmentDto`, `CreateAssignmentRequest`,
      `TransitionRequest`.
- [ ] Backend Flyway `V4__assignments.sql`: `assignments(id, request_id fk unique-ish,
      rider_id fk users, dispatcher_id null fk users, status, assigned_at, accepted_at,
      collected_at, delivered_at, completed_at)`.
- [ ] Backend `feature/assignment`: `POST /assignments` (dispatcher; sets request ASSIGNED),
      `GET /assignments?role=...` (rider sees own, dispatcher sees tenant), 
      `POST /assignments/{id}/transition` (validates allowed transition + actor role,
      updates request status accordingly).
- [ ] Server-side state machine guarding legal transitions and who may perform them.
- [ ] App data/domain: `AssignmentRepository`; use cases `AssignRider`, `GetRiderTasks`,
      `GetDispatchQueue`, `TransitionAssignment`.
- [ ] Dispatcher Compose: `DispatchQueueScreen` (PENDING requests), rider picker, assign action.
- [ ] Rider Compose: `RiderTasksScreen` (inbox), `TaskDetailScreen` with accept + status-step
      controls; clear status chips (StatusColors); loading/empty/error.
- [ ] Wire into Dispatcher and Rider home navigation.

## Data-model changes (Postgres)
- `V4__assignments.sql` (above). Update `requests.status` on transitions (in the same
  transaction/service).

## API endpoints
- `POST /assignments` · `GET /assignments` · `POST /assignments/{id}/transition`.

## Acceptance criteria
- [ ] Dispatcher assigns a PENDING request to a rider → request becomes ASSIGNED, assignment
      row created.
- [ ] Rider sees the task, accepts, and walks it COLLECTED → DELIVERED → COMPLETED; request
      status mirrors each step.
- [ ] Illegal transitions (e.g., COMPLETED→ASSIGNED, wrong rider) are rejected server-side.
- [ ] Scoping: a rider sees only their tasks; a dispatcher only their tenant's queue.

## Handoff notes (fill when done)
- Final transition state machine + who-can-do-what: _____
- Auto-assign implemented? rule used: _____
- How "available riders" are determined: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
