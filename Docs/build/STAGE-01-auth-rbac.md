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
- [x] Add `Role` enum to `:shared` (SUPER_ADMIN, MPS_ADMIN, MPS_DISPATCHER, SCHOOL_ADMIN,
      SCHOOL_STAFF, RIDER, CONTRIBUTOR) + `AuthDto`s (`MeResponse`, etc.).
- [x] Firebase Auth wiring in `:app` data layer: `AuthRepository` (register/login/logout/
      currentUser/idToken) implementing a domain interface.
- [x] DataStore session store (`SessionDataStore`): persist signed-in flag + cached `MeResponse`.
- [x] `AuthInterceptor` now pulls a fresh Firebase ID token for each request (`FirebaseTokenProvider`).
- [x] Use cases: `RegisterUser`, `LoginUser`, `LogoutUser`, `GetCurrentSession`.
- [x] Compose: `LoginScreen`, `RegisterScreen`, `AuthViewModel`/`UiState`; field validation,
      inline errors, disabled+spinner on submit.
- [x] Backend Flyway `V2__users.sql`: `users(id, firebase_uid unique, name, email, role,
      tenant_id null, phone, created_at)`.
- [x] Backend `feature/auth`: `GET /auth/me` (verify token → upsert user → return profile),
      set custom claims via Firebase Admin; role-require helper for routes.
- [x] App: on login, call `/auth/me`, cache result, navigate by role via `RootNavGraph`.
- [x] Stub per-role home screens (just a titled scaffold + logout) to land navigation.
- [x] Document first-super-admin bootstrap (e.g., env-listed email promoted on first `/auth/me`).

## Data-model changes (Postgres)
- `V2__users.sql` — `users` table (see above). FK `tenant_id` references `tenants` once that
  exists (Stage 1 minimal tenants table may be added here or stubbed nullable until Stage 7).

## API endpoints
- `GET /auth/me` — verify Firebase token, upsert+return user, set custom claims.
- (optional) `POST /auth/register-profile` if profile data is captured at signup.

## Acceptance criteria
- [x] New user can register, then log in; session survives app restart (DataStore).
- [x] `users` row exists in Postgres with correct role; Firebase custom claims set.
- [x] App routes each role to its (stub) home; logout returns to login.
- [x] A protected backend route rejects requests without a valid token (401) and enforces role.
- [x] First super_admin can be established via the documented bootstrap.

## Handoff notes
- **Role assignment at registration:** New users default to `CONTRIBUTOR`. The bootstrap check
  runs on every `/auth/me` call while the user is still `CONTRIBUTOR`, and promotes to
  `SUPER_ADMIN` only when ALL of: (a) `SUPER_ADMIN_EMAIL` env var is set and matches the
  caller's email (case-insensitive), (b) Firebase reports `emailVerified = true` for that
  token, (c) no `SUPER_ADMIN` row exists yet (single-use). Bootstrap flow: register → click
  verification link in email → sign back in (or force-refresh token) → call `/auth/me` →
  promoted. All other role changes are done via Stage 7 admin user management.
- **Custom-claims propagation:** Claims are set server-side after `/auth/me`. The client must
  call `firebaseUser.getIdToken(true)` to force-refresh the token before the new claims appear
  in subsequent requests. For Stage 1 this is not needed (we use Postgres role, not claims, for
  routing), but Stage 3+ backend routes that read the `role` claim must document this.
- **Display name in JWT:** After registration, `updateProfile` is called, then the token is
  force-refreshed (`getIdToken(true)`) before `/auth/me` so the backend receives the `name`
  claim.
- **Stub home routes created per role:**
  - `SuperAdminHomeScreen` → `home/super_admin`
  - `MpsAdminHomeScreen` → `home/mps_admin`
  - `MpsDispatcherHomeScreen` → `home/mps_dispatcher`
  - `SchoolAdminHomeScreen` → `home/school_admin`
  - `SchoolStaffHomeScreen` → `home/school_staff`
  - `RiderHomeScreen` → `home/rider`
  - `ContributorHomeScreen` → `home/contributor`
- **Session load splash:** `MainActivity` drives `MainViewModel` which reads `GetCurrentSession`
  (a DataStore Flow). Shows `CircularProgressIndicator` until first emission, then routes.

## Resume / progress
- **Status:** ✅ Complete — all tasks and acceptance criteria met.
- **Next stage:** Stage 2 — Core Request System (`STAGE-02-core-request.md`).

## Status
✅ Done.
