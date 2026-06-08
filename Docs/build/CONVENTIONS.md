# I-Sujana — Conventions (stable rules, read once per chat)

These rules are constant across all stages. Follow them; don't reinvent per stage.

---

## 1. Architecture (Clean Architecture + MVVM)

Three layers in `:app`, dependencies point **inward** (ui → domain ← data):

- **domain/** — pure Kotlin. `entities` (core models), `repository` (interfaces),
  `usecase` (one class per business action, `operator fun invoke(...)`). No Android/Firebase
  imports here.
- **data/** — implements domain repository interfaces. `remote` (Retrofit services + DTO
  mappers), `local` (Room DAOs, DataStore), `repository` (impl that combines remote+local).
  Maps DTO/Entity ⇄ domain models; domain never sees DTOs.
- **feature/<name>/** — `ui` (Compose screens + components), `<Name>ViewModel`,
  `<Name>UiState`. ViewModels depend on use cases, never on repositories or Retrofit directly.

`core/` holds cross-cutting: `theme`, `di` (Hilt modules), `network` (OkHttp/Retrofit + auth
interceptor), `common` (Result wrapper, extensions, constants).

**Module boundaries:** `:app` and `:backend` both depend on `:shared` for wire DTOs/enums.
`:app` never depends on `:backend`. `:shared` is pure Kotlin (no Android, no Ktor, no Firebase).

---

## 2. Tech & tooling

- **Kotlin + Coroutines/Flow** everywhere. ViewModels expose `StateFlow<UiState>`.
- **KSP only** — never kapt (Hilt, Room compilers via KSP).
- **Version catalog is the single source of versions** — add every dependency/version/plugin
  to `gradle/libs.versions.toml`; reference via `libs.*`. Never hardcode versions in
  `build.gradle.kts`.
- **Hilt** for DI (`@HiltViewModel`, `@Module @InstallIn`). One Hilt module per concern.
- **Compose + Material 3** for all UI. Navigation Compose for navigation.
- **Serialization:** `kotlinx.serialization` for DTOs in `:shared` (used by both Retrofit
  via the kotlinx converter and Ktor `ContentNegotiation`).

---

## 3. App ↔ Backend contract

- App talks to backend over **Retrofit + OkHttp** (`core/network`). Base URL is a build
  config field (local dev vs deployed).
- **Auth:** an OkHttp `Interceptor` attaches the current **Firebase ID token** as
  `Authorization: Bearer <token>` on every request. Backend verifies it with the Firebase
  Admin SDK, resolves the user from Postgres, and authorizes by role.
- **DTOs live in `:shared`** so request/response shapes can't drift between client and server.
- **Errors:** backend returns a consistent JSON error body `{ "error": { "code", "message" } }`
  with proper HTTP status. App maps non-2xx to a sealed `AppError`; never swallow silently.
- **IDs:** UUID (string) primary keys for portability between Postgres and Firebase paths.
- **Time:** store/transmit UTC ISO-8601; format for display in the app.

---

## 4. Backend (Ktor) conventions

- Structure by feature: `feature/<name>/` with `<Name>Routes.kt` + `<Name>Service.kt`.
  Routes are thin; logic in services; DB access via Exposed in services/repositories.
- **Migrations via Flyway** in `src/main/resources/db/migration` (`V1__init.sql`, ...).
  Never mutate schema by hand; every schema change is a new versioned migration.
- Firebase Admin verification lives in an auth plugin; protected routes use an
  `authenticate` block and read the resolved user from the call principal.
- Config (DB URL, Firebase creds, ports) via environment variables — never commit secrets.

---

## 5. Data integrity & RBAC

- **Postgres is the source of truth** for users, tenants, schools, requests, assignments,
  notifications, audit logs. Firebase **custom claims** (`role`, `tenantId`) are a mirror
  for fast client/Realtime-DB gating — Postgres wins on conflict.
- Every mutating endpoint enforces role + tenant scoping server-side. Never trust the client.
- Realtime DB (tracking) is guarded by security rules keyed on custom claims.
- Image/file uploads go to **Cloudinary** (not Firebase Storage — not free). Cloudinary access uses signed upload presets; credentials live in env vars, never in the client APK.

---

## 6. UI conventions

- Consume design tokens from `DESIGN-SYSTEM.md` (implemented in `core/theme`). **No raw hex**
  in screens — use `MaterialTheme.colorScheme` / typography / spacing tokens.
- Every screen handles **loading / empty / error / content** states explicitly.
- Lucide-style icon set, one family. No emoji as icons. Touch targets ≥ 48dp.
- Lists with 50+ items use `LazyColumn` (and Paging 3 once Stage 9 lands).

---

## 7. Testing

- **domain** use cases: plain JUnit unit tests (no Android).
- **data** repositories: unit tests with fakes/mockk; map-layer tests.
- **ViewModels**: turbine + coroutine-test for state emissions.
- **backend**: Ktor `testApplication` integration tests against a test Postgres (Testcontainers
  or an in-memory/embedded Postgres) — wired in Stage 10, but write tests as you build.
- Don't claim a stage done until its acceptance checks actually pass (run them).

---

## 8. Git & commits

- Conventional-commit style: `feat:`, `fix:`, `chore:`, `docs:`, scoped where useful
  (`feat(auth): ...`). Keep commits focused per task.
- Commit/push only when the user asks. The repo is not yet a git repo (Stage 0 may `git init`
  if the user wants).

---

## 9. Naming

- Packages lowercase; classes PascalCase; functions/vals camelCase; Compose screens
  `XxxScreen`, state `XxxUiState`, viewmodels `XxxViewModel`, use cases verb-first
  (`CreatePickupRequest`), Exposed tables `XxxTable`, DTOs `XxxDto`/`XxxRequest`/`XxxResponse`.
- Enums shared across tiers (roles, statuses) live in `:shared`.
