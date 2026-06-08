# STAGE 01 — Auth & RBAC

**Goal:** Users can register and sign in with Firebase Auth; the backend resolves them from
Postgres, assigns a role, mirrors role/tenant into Firebase custom claims, and the app routes
each role to its home. All API calls carry a verified ID token.

**Prerequisites:** Stage 0 ✅ (monorepo, theme, network skeleton, Firebase, Ktor+Postgres).

---

## In scope
- Firebase Auth email/password: register + login + logout + password reset.
- Login/Register Compose screens (per `DESIGN-SYSTEM.md`), form validation, loading/error states.
- Session persistence in DataStore (signed-in state, cached profile/role).
- `users` table + `Role` enum (7 roles) in Postgres; `GET /auth/me` upsert/read.
- Backend sets Firebase **custom claims** `{role, tenantId}` after resolving the user.
- Real `AuthInterceptor` token source (current Firebase user's fresh ID token).
- Role-based navigation: a `RootNavGraph` that sends each role to its home scaffold (stub
  home screens per role are fine — they fill in later stages).
- Backend auth middleware: protected routes read the resolved user principal; helper to
  require specific roles.

## Out of scope (defer)
- Admin creating/managing other users → **Stage 7**. (For now, role can be assigned via a
  default rule + a seed/admin bootstrap; document how the first super_admin is created.)
- Phone/social auth → not planned unless user asks.
- Rich profile editing → later/Stage 7.

---

## Task checklist
- [ ] Add `Role` enum to `:shared` (SUPER_ADMIN, MPS_ADMIN, MPS_DISPATCHER, SCHOOL_ADMIN,
      SCHOOL_STAFF, RIDER, CONTRIBUTOR) + `AuthDto`s (`MeResponse`, etc.).
- [ ] Firebase Auth wiring in `:app` data layer: `AuthRepository` (register/login/logout/
      currentUser/idToken) implementing a domain interface.
- [ ] DataStore session store (`SessionRepository`): persist signed-in flag + cached `MeResponse`.
- [ ] `AuthInterceptor` now pulls a fresh Firebase ID token for each request.
- [ ] Use cases: `RegisterUser`, `LoginUser`, `LogoutUser`, `GetCurrentSession`.
- [ ] Compose: `LoginScreen`, `RegisterScreen`, `AuthViewModel`/`UiState`; field validation,
      inline errors, disabled+spinner on submit.
- [ ] Backend Flyway `V2__users.sql`: `users(id, firebase_uid unique, name, email, role,
      tenant_id null, phone, created_at)`.
- [ ] Backend `feature/auth`: `GET /auth/me` (verify token → upsert user → return profile),
      set custom claims via Firebase Admin; role-require helper for routes.
- [ ] App: on login, call `/auth/me`, cache result, navigate by role via `RootNavGraph`.
- [ ] Stub per-role home screens (just a titled scaffold + logout) to land navigation.
- [ ] Document first-super-admin bootstrap (e.g., env-listed email promoted on first `/auth/me`).

## Data-model changes (Postgres)
- `V2__users.sql` — `users` table (see above). FK `tenant_id` references `tenants` once that
  exists (Stage 1 minimal tenants table may be added here or stubbed nullable until Stage 7).

## API endpoints
- `GET /auth/me` — verify Firebase token, upsert+return user, set custom claims.
- (optional) `POST /auth/register-profile` if profile data is captured at signup.

## Acceptance criteria
- [ ] New user can register, then log in; session survives app restart (DataStore).
- [ ] `users` row exists in Postgres with correct role; Firebase custom claims set.
- [ ] App routes each role to its (stub) home; logout returns to login.
- [ ] A protected backend route rejects requests without a valid token (401) and enforces role.
- [ ] First super_admin can be established via the documented bootstrap.

## Handoff notes (fill when done)
- How role is assigned at registration (default + bootstrap rule): _____
- Custom-claims propagation note (client must refresh token after claims set): _____
- Stub home routes created per role (names): _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
