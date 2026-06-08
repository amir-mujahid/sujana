# STAGE 07 — Admin & Tenant Management

**Goal:** Full administrative management of the org hierarchy and users, with audit logging:
Super Admin manages MPS tenants; MPS Admin manages schools, waste points, and users in their
tenant; School Admin manages school staff. Replaces the minimal/seeded data from earlier stages.

**Prerequisites:** Stage 0–4 ✅ (needs auth/roles, requests; can run before or after 5/6).

---

## In scope
- Full `tenants`, `schools`, `waste_points` tables + CRUD (enriching minimal versions).
- User management: create/invite, assign role + tenant/school, deactivate.
- `audit_logs` table + middleware to record mutating admin actions.
- Role-scoped admin screens:
  - **Super Admin:** manage MPS tenants, global user list, system overview.
  - **MPS Admin:** manage schools + waste points + users in tenant.
  - **School Admin:** manage school staff + the school's waste points.
- Server-side authorization for every admin mutation (role + tenant scope).

## Out of scope (defer)
- Analytics dashboards/charts → **Stage 8**.
- Bulk import/export → optional later.

---

## Task checklist
- [ ] Flyway `V7__admin.sql`: finalize `tenants`, `schools`, `waste_points`; add
      `users.active`, FK constraints; `audit_logs(id, actor_id, action, entity, entity_id,
      meta_json, created_at)`.
- [ ] Backend `feature/admin`: CRUD routes for tenants/schools/waste-points/users, each guarded
      by required role + tenant scope; audit-log middleware/hook on mutations.
- [ ] User management endpoints: create (with Firebase Auth user creation via Admin SDK or
      invite flow), set role/tenant (updates custom claims), deactivate.
- [ ] App data/domain: admin repositories + use cases per entity.
- [ ] Compose admin screens per role (lists with create/edit/deactivate; forms per
      `DESIGN-SYSTEM.md`; confirm dialogs for destructive actions; tabular data lists).
- [ ] Audit log viewer (Super Admin / MPS Admin) — paged, filterable list.
- [ ] Migrate any seeded schools from earlier stages into managed data.

## Data-model changes (Postgres)
- `V7__admin.sql` — full `tenants`/`schools`/`waste_points`, `users.active`, `audit_logs`.

## API endpoints
- `…/tenants` CRUD · `…/schools` CRUD · `…/waste-points` CRUD · `…/users` CRUD +
  role/tenant assignment + deactivate · `GET /audit-logs` (scoped, paged).

## Acceptance criteria
- [ ] Super Admin can create/edit MPS tenants and see all users; MPS Admin manages their
      tenant's schools/waste-points/users; School Admin manages their staff/waste-points.
- [ ] Role/tenant changes propagate to Firebase custom claims; deactivated users can't act.
- [ ] Mutating admin actions appear in `audit_logs` with actor + entity.
- [ ] All admin mutations are rejected when attempted outside the actor's role/tenant scope.

## Handoff notes (fill when done)
- User creation approach (Admin SDK direct vs invite/email): _____
- Audit-log coverage (which actions): _____
- Custom-claims refresh strategy after role change: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
