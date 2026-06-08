# STAGE 10 — Observability · Testing · QA · Deploy

**Goal:** Ship-ready: instrument the app, achieve meaningful test coverage across layers, do a
final QA pass, and deploy the backend so the app runs against a hosted API.

**Prerequisites:** Stage 0–9 ✅ (the whole system, hardened).

---

## In scope

### Observability
- Firebase **Crashlytics** (app) — verify crash reporting + non-fatals.
- Firebase **Analytics** — key funnels (request created, assigned, completed, login).
- **Performance Monitoring** — startup + key network traces.
- Backend structured logging + **request trace IDs** propagated from app (correlate a request
  across app→API→DB); basic metrics/health.

### Testing & QA
- Unit tests: domain use cases (app), services (backend).
- Repository/data-layer tests with fakes.
- ViewModel state tests (turbine + coroutine-test).
- Backend integration tests (Ktor `testApplication` + Testcontainers/embedded Postgres).
- A few critical UI/Compose tests (login, create request, rider transition).
- Manual QA pass over both workflows on real devices; accessibility + light/dark check.

### Deploy
- Containerize `:backend`; deploy to **Cloud Run** (free tier) — or **Render/Railway** fallback
  if no billing card. Postgres = **Neon** (promote dev branch / create prod branch).
- App points to the deployed base URL for release; secrets via env; Firebase prod config.

## Out of scope (defer)
- Play Store release process (separate effort if desired).
- SIEM / advanced enterprise observability (note as future).

---

## Task checklist
- [ ] Add Crashlytics, Analytics, Performance Monitoring; verify events/crashes arrive.
- [ ] Trace-ID header generated in app, logged through backend → DB layer.
- [ ] Domain/use-case unit tests (app) + service unit tests (backend).
- [ ] Data-layer + ViewModel tests; mappers covered.
- [ ] Backend integration tests against a test Postgres (Testcontainers/embedded).
- [ ] Critical Compose UI tests (login, create request, rider status transition).
- [ ] Manual QA: full Contributor flow + full School→MPS flow on real devices; a11y, light/dark,
      reduced-motion, large font.
- [ ] Containerize backend (Dockerfile); deploy to Cloud Run (or Render/Railway); set env/secrets.
- [ ] Neon prod branch; run Flyway migrations on deploy; smoke-test `/health` + an auth flow.
- [ ] Release build of app pointing at deployed API; final regression.

## Data-model changes (Postgres)
- None (or a final cleanup migration). Run all migrations against the prod Neon branch.

## API endpoints
- No new endpoints; ensure all are documented and covered by tests.

## Acceptance criteria
- [ ] Crashlytics shows a forced test crash; Analytics shows key events; Perf traces appear.
- [ ] Test suites pass (`./gradlew test` app + backend; integration tests green).
- [ ] Both end-to-end workflows pass manual QA on a real device.
- [ ] Backend is deployed and reachable; app release build works against it; `/health` green
      with prod DB.

## Handoff notes (fill when done)
- Deploy target used + URL: _____
- Test coverage summary + how to run: _____
- Known issues / follow-ups: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started — final stage. Project complete when this is ✅.
