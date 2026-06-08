# STAGE 06 — School → MPS Workflow

**Goal:** Implement the second workflow: a School Admin/Staff submits a waste pickup request
to their MPS, the MPS Dispatcher schedules and assigns a rider, and collection proceeds —
reusing the existing request/assignment/tracking/notification infrastructure.

**Prerequisites:** Stage 0–5 ✅ (requests, assignments, tracking, notifications all exist).

---

## In scope
- School-side submission UI for `RequestType.SCHOOL` (reuses `requests` table/endpoints).
- Scheduling: a requested pickup date/time window on school requests.
- MPS Dispatcher view filtered to incoming SCHOOL requests for their tenant; assign rider
  (reuse Stage 3 assignment flow).
- School Admin/Staff tracking + status view (reuse Stage 4 tracking, Stage 5 notifications).
- Tenant scoping so schools only see their MPS and vice-versa.

## Out of scope (defer)
- Full school/waste-point management CRUD → **Stage 7**.
- Recurring/scheduled pickups automation → optional later; one-off scheduling here.

---

## Task checklist
- [ ] Extend `requests` for scheduling: Flyway `V6__school_requests.sql` adding
      `scheduled_for null, school_id null fk schools` (and any school-request fields).
- [ ] Backend: allow SCHOOL-type request creation by school roles; dispatcher list filter by
      type=SCHOOL + tenant; ensure tenant scoping throughout.
- [ ] App domain/data: extend request use cases / repository for school submission + schedule.
- [ ] School Compose: `SchoolRequestScreen` (submit, choose waste point/location + schedule),
      `SchoolRequestsScreen` (track status).
- [ ] Dispatcher: incoming SCHOOL requests tab in the dispatch queue; assign as in Stage 3.
- [ ] Hook notifications (Stage 5) for school events (received/scheduled/assigned/collected).
- [ ] Wire into School Admin/Staff home navigation.

## Data-model changes (Postgres)
- `V6__school_requests.sql` — scheduling columns on `requests`; ensure `school_id`/`tenant_id`
  linkage for scoping.

## API endpoints
- Reuse `POST /requests` (type=SCHOOL), `GET /requests` (filter type + tenant). Add filters/
  query params as needed; no major new endpoints expected.

## Acceptance criteria
- [ ] School Admin/Staff submits a SCHOOL pickup request with a schedule; it appears in the
      correct MPS dispatcher's queue (tenant-scoped).
- [ ] Dispatcher assigns a rider; school can track status + live location and gets notifications.
- [ ] Contributor and School workflows coexist without regressions.

## Handoff notes (fill when done)
- Scheduling model (single time vs window): _____
- Tenant-scoping rules confirmed for school↔MPS: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
