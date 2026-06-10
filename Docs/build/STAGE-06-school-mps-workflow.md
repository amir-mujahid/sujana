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
- [x] Extend `requests` for scheduling: Flyway `V6__school_requests.sql` adding
      `scheduled_for null, school_id null fk schools` (and any school-request fields).
- [x] Backend: allow SCHOOL-type request creation by school roles; dispatcher list filter by
      type=SCHOOL + tenant; ensure tenant scoping throughout.
- [x] App domain/data: extend request use cases / repository for school submission + schedule.
- [x] School Compose: `SchoolRequestScreen` (submit, choose waste point/location + schedule),
      `SchoolRequestsScreen` (track status).
- [x] Dispatcher: incoming SCHOOL requests tab in the dispatch queue; assign as in Stage 3.
- [x] Hook notifications (Stage 5) for school events (received/scheduled/assigned/collected).
- [x] Wire into School Admin/Staff home navigation.

## Data-model changes (Postgres)
- `V6__school_requests.sql` — scheduling columns on `requests`; ensure `school_id`/`tenant_id`
  linkage for scoping.

## API endpoints
- Reuse `POST /requests` (type=SCHOOL), `GET /requests` (filter type + tenant). Add filters/
  query params as needed; no major new endpoints expected.

## Acceptance criteria
- [x] School Admin/Staff submits a SCHOOL pickup request with a schedule; it appears in the
      correct MPS dispatcher's queue (tenant-scoped).
- [x] Dispatcher assigns a rider; school can track status + live location and gets notifications.
- [x] Contributor and School workflows coexist without regressions.

## Handoff notes (fill when done)
- Scheduling model: single optional date + optional time (ISO-8601 OffsetDateTime stored as `scheduled_for`). Both nullable; no enforcement of future-only constraint (leave that for Stage 9 hardening).
- Tenant-scoping rules confirmed for school↔MPS: `users.tenant_id` → `schools.tenant_id` join. Dispatcher sees CONTRIBUTOR requests (all) + SCHOOL requests where `requester_school_id` belongs to their tenant's schools. SCHOOL_ADMIN sees all requests for their school's `requester_school_id`. SCHOOL_STAFF sees their own requests (by `requester_id`).
- `AssignmentService.getAssignment()` now allows SCHOOL_ADMIN/SCHOOL_STAFF to access their request's assignment (needed for live tracking navigation).
- Dispatcher is notified via FCM+WS when a SCHOOL request is created (`notifyDispatchersNewRequest`). School requester is now notified when dispatcher assigns a rider (added to `AssignmentRoutes POST /assignments`).
- `SCHOOL_REQUEST_DETAIL` route reuses `RequestDetailScreen` + `RequestDetailViewModel`; no deeplink registered (avoids duplicate with contributor route; FCM deeplinks for school users land on contributor route which works because `getRequest` is owner-aware).

## Resume / progress
_Stage complete._

## Status
✅ Done.
