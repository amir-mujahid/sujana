# STAGE 08 — Analytics Dashboard

**Goal:** Give MPS Admin/Dispatcher (and Super Admin) operational insight via aggregate
reporting computed in Postgres and rendered as charts — the payoff of choosing a relational
system of record.

**Prerequisites:** Stage 0–7 ✅ (needs real requests/assignments/tenants data to aggregate).

---

## In scope
- Backend reporting endpoints with server-side aggregation (SQL `GROUP BY`/window functions):
  - Requests over time (by day/week), by status, by type.
  - Completion rate, average pickup→completed duration, overdue/SLA counts.
  - Rider throughput / active riders; per-school and per-tenant breakdowns.
- Dashboard screens with charts (trend = line, comparison = bar, proportion = donut sparingly)
  following the chart guidelines (legends, tooltips, accessible colors, empty/loading states).
- Date-range + tenant/school filters; Super Admin sees cross-tenant, MPS Admin sees own tenant.

## Out of scope (defer)
- Data warehouse / BigQuery export → optional future; Postgres aggregates suffice now.
- CSV/PDF export → optional (add if time allows).

---

## Task checklist
- [ ] Decide a charts approach for Compose (e.g., a Compose charts lib in the catalog or
      custom Canvas) — keep it lightweight and on-theme.
- [ ] Add DB indexes that support the aggregate queries (also revisited in Stage 9).
- [ ] Backend `feature/analytics`: endpoints returning pre-aggregated, role/tenant-scoped DTOs
      (`GET /analytics/summary`, `/analytics/requests-trend`, `/analytics/rider-throughput`,
      etc.) with date-range + scope params.
- [ ] App data/domain: analytics repository + use cases; cache short-term where sensible.
- [ ] Compose: `DashboardScreen` with metric cards (tabular figures) + charts; filters;
      empty/loading/error per chart.
- [ ] Wire into MPS Admin / Dispatcher / Super Admin homes.

## Data-model changes (Postgres)
- No new core tables required; add supporting **indexes** and possibly a materialized view or
  summary table if a query is heavy (document any added).

## API endpoints
- `GET /analytics/summary` · `GET /analytics/requests-trend` ·
  `GET /analytics/rider-throughput` · (others as needed) — all scoped + date-ranged.

## Acceptance criteria
- [ ] Dashboard shows correct aggregates that match the underlying data for a chosen range.
- [ ] Charts render with legends/labels, handle empty + loading, and use accessible colors
      (not color-alone).
- [ ] Scoping holds: MPS Admin sees only their tenant; Super Admin sees all.
- [ ] Aggregates are computed server-side (no large raw pulls to the client).

## Handoff notes (fill when done)
- Charts library/approach chosen: _____
- Indexes / materialized views added: _____
- Metric definitions (e.g., how "completion time" is measured): _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
