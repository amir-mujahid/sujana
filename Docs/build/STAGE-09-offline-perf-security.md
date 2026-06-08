# STAGE 09 — Offline · Performance · Security Hardening

**Goal:** Make the app resilient and fast, and harden the system end-to-end before final QA.
This is a cross-cutting hardening pass over everything built in Stages 0–8.

**Prerequisites:** Stage 0–8 ✅ (hardening the full feature set).

---

## In scope

### Offline & performance (app)
- Room caching for key read models (requests, assignments, notifications) with a
  single-source-of-truth Flow (cache → network refresh).
- **Paging 3** for long lists (requests, dispatch queue, notifications, audit logs).
- Baseline Profiles for faster startup; lazy loading; image optimization via Coil.
- WorkManager for background sync / retrying queued writes made while offline.
- Offline state messaging + graceful degradation.

### Performance (backend / DB)
- Postgres indexing for hot queries; avoid N+1; select only needed columns; batch writes.
- Pagination on all list endpoints; query monitoring for slow queries.

### Security hardening
- Tighten Firebase Security Rules (RTDB) by custom claims. Verify Cloudinary signed upload presets (no unsigned public uploads).
- Backend input validation on every endpoint; consistent error contract; rate limiting on
  sensitive endpoints.
- Secrets only via env; no keys in the repo; restrict the Maps key (app + API restrictions).
- R8/ProGuard: enable minify + shrink for release; verify rules don't break reflection
  (Retrofit/serialization/Room).
- Optional: certificate pinning; encrypt sensitive DataStore values.

## Out of scope (defer)
- Crashlytics/Analytics/tests → **Stage 10** (observability & QA).
- Play Integrity / advanced anti-tamper → optional enterprise extras (note if added).

---

## Task checklist
- [ ] Room entities + DAOs for cached read models; repositories emit cache-first Flows.
- [ ] Paging 3 on long lists (app) backed by paged endpoints (backend) — verify both ends.
- [ ] WorkManager sync worker(s) for offline-created actions; conflict handling documented.
- [ ] Baseline Profile generation wired; measure startup before/after.
- [ ] Backend: add/verify indexes for analytics + list queries; eliminate N+1; paginate all lists.
- [ ] Validation layer on all endpoints; rate limiting on auth/sensitive routes; tighten error
      bodies (no leaks).
- [ ] Firebase RTDB rules reviewed against custom claims. Cloudinary signed upload presets verified.
- [ ] Maps API key restricted (Android app signing SHA + API scope).
- [ ] Enable R8 minify/shrink for release; add keep rules as needed; verify release build runs.
- [ ] (Optional) cert pinning + DataStore encryption.

## Data-model changes (Postgres)
- Indexes / minor schema tweaks only (new Flyway migration `V8__indexes.sql` as needed).

## API endpoints
- No new endpoints; add pagination params + validation to existing ones.

## Acceptance criteria
- [ ] App usable offline for cached reads; queued writes sync when back online.
- [ ] Long lists page smoothly (no loading all rows); measurable startup improvement.
- [ ] Release build (R8 on) runs correctly with no missing-keep crashes.
- [ ] Security rules + backend validation reject unauthorized/invalid requests; Maps key restricted.
- [ ] Slow queries identified and indexed; no obvious N+1 in hot paths.

## Handoff notes (fill when done)
- What's cached in Room + sync strategy: _____
- Indexes added + measured query improvements: _____
- R8 keep rules added; security rules summary: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
