# STAGE 00 — Foundation & Infrastructure

**Goal:** Turn the stock single-module Android template into the monorepo skeleton
(`:app` Compose + Clean Arch, `:backend` Ktor, `:shared` DTOs), wire all tooling via the
version catalog, implement the enterprise theme, and stand up a runnable backend connected
to Postgres. No features yet — just a solid, buildable base everything else plugs into.

**Prerequisites:** none (this is the first stage).

---

## In scope
- Gradle monorepo: add `:backend` and `:shared` modules; keep `:app`.
- Convert `:app` to Kotlin + Jetpack Compose + Material 3 (replace the Views template).
- Expand `gradle/libs.versions.toml` with every dependency we'll need (see checklist).
- KSP + Hilt set up in `:app`.
- Clean Architecture package skeleton (`core/`, `data/`, `domain/`, `feature/`).
- Implement `DESIGN-SYSTEM.md` as the Material 3 theme (color schemes, Inter typography,
  spacing tokens, StatusColors) in `core/theme`.
- Networking skeleton (`core/network`): Retrofit + OkHttp + Firebase ID-token interceptor +
  base URL build config field (no real endpoints yet beyond health).
- Firebase project created; `google-services.json` added; Firebase BoM wired (Auth, RTDB,
  FCM, Crashlytics deps present, not yet used). **No Firebase Storage** — Cloudinary used instead.
- `:backend` Ktor app: serialization, status pages, Firebase Admin token-verify plugin,
  Exposed + DB connection, Flyway baseline migration, `/health` endpoint.
- `:shared` module with a couple of placeholder DTOs/enums to prove cross-module sharing.
- Local Postgres (or Neon dev branch) connection documented via env vars.

## Out of scope (defer)
- Any real auth/login UI or user table beyond what `/health` needs → **Stage 1**.
- Any business feature, screens beyond a themed placeholder → **Stage 1+**.
- Deploying the backend anywhere → **Stage 10** (run locally for now).

---

## Task checklist
- [x] Update `settings.gradle.kts` to `include(":app", ":backend", ":shared")`.
- [x] Add Kotlin, KSP, Hilt, Compose, kotlinx-serialization, google-services plugins to the
      catalog `[plugins]` and root `build.gradle.kts` (`apply false`).
- [x] Populate `[versions]`/`[libraries]` in `libs.versions.toml`: Compose BoM, Material3,
      Activity-Compose, Navigation-Compose, Lifecycle/ViewModel, Hilt + hilt-navigation-compose,
      Coroutines, Retrofit + OkHttp + kotlinx-serialization-converter, Room (+ksp), DataStore,
      Coil, WorkManager, Paging3, maps-compose + play-services-maps + play-services-location,
      Firebase BoM (auth, database, messaging, storage, crashlytics, analytics).
- [x] Convert `:app` to Compose: Kotlin+Compose plugins, `buildFeatures { compose = true }`,
      `MainActivity` with `setContent { SujanaTheme { ... } }`, remove Views/appcompat leftovers.
- [x] Create package skeleton: `com.sujana.core.{theme,di,network,common}`,
      `com.sujana.{data,domain}`, `com.sujana.feature`.
- [x] Implement `core/theme`: light+dark `ColorScheme`, Inter `Typography`, `Spacing` tokens,
      `StatusColors` per `DESIGN-SYSTEM.md`; `SujanaTheme` composable.
- [x] Hilt: `@HiltAndroidApp Application`, `@AndroidEntryPoint MainActivity`, base DI modules, `hilt-navigation-compose` wired.
      **Resolved:** Hilt 2.59.2 is compatible with AGP 9.2.1. Plugin + KSP compiler fully active.
- [x] `core/network`: Retrofit/OkHttp providers, `AuthInterceptor` (Bearer Firebase ID token —
      stub token source until Stage 1), `BASE_URL` build config field (debug = local backend).
- [x] Add Firebase: placeholder `google-services.json` added (replace with real file from
      Firebase Console — register app with package `com.sujana`), google-services plugin applied,
      Firebase BoM + all required SDKs declared as dependencies.
- [x] Scaffold `:shared` (pure Kotlin/JVM): `enum Role` + `HealthDto`; kotlinx-serialization
      applied. `:app` and `:backend` both depend on `:shared` — verified at compile time.
- [x] Scaffold `:backend` Ktor: `Application.kt`, ContentNegotiation (kotlinx),
      StatusPages, Exposed `Database.connect` from env (`DATABASE_URL`), Flyway `V1__baseline.sql`,
      Firebase Admin init + token-verify plugin (bearer auth), `GET /health` returning `HealthDto`.
- [x] Document env vars + local Postgres/Neon setup in `backend/README.md`.

## Data-model changes (Postgres)
- `V1__baseline.sql` — creates `_sujana_meta` table (proves connectivity). Real tables Stage 1+.

## API endpoints
- `GET /health` → `{ status, dbConnected, time }`.

## Acceptance criteria
- [x] `./gradlew :app:assembleDebug` builds — **VERIFIED BUILD SUCCESSFUL**.
- [ ] App launches showing a themed placeholder screen (light + dark both render).
      _Cannot verify without device/emulator — confirm manually in Android Studio._
- [x] `./gradlew :backend:compileKotlin` succeeds — **VERIFIED BUILD SUCCESSFUL**.
      `:backend:run` needs DB env vars — run with `backend/README.md` env var setup.
- [x] `:shared` DTO/enum referenced from both `:app` and `:backend` — verified at compile time.
- [x] No kapt anywhere; all versions resolved through the catalog.

## Handoff notes
- **Backend local URL:** `http://10.0.2.2:8080` (Android emulator) / `http://localhost:8080` (device on same Wi-Fi)
- **Firebase:** placeholder `google-services.json` is in `app/`. Replace with real file from
  Firebase Console — create project, register app with package `com.sujana`.
  Enable: Auth (Email/Password + Google), Realtime DB, FCM, Crashlytics, Analytics.
  **Do NOT enable Firebase Storage** — too expensive; use Cloudinary instead.
- **Postgres:** local (`jdbc:postgresql://localhost:5432/sujana_dev`) or Neon dev branch.
  See `backend/README.md` for all env vars.
- **Hilt AGP 9.x:** ~~Blocker~~ **Resolved.** Hilt 2.59.2 (`com.google.dagger:hilt-compiler` artifact) is confirmed compatible with AGP 9.2.1. Plugin active, `@HiltAndroidApp` and `@AndroidEntryPoint` restored. DI fully wired.
- **Inter font TODO:** `core/theme/Typography.kt` uses `FontFamily.Default` as a placeholder.
  Replace with bundled Inter `.ttf` assets or `ui-text-google-fonts` + GMS cert XML in Stage 1 or 2.
- **AGP 9.x Kotlin:** `android.disallowKotlinSourceSets=false` is set in `gradle.properties`
  to allow KSP to register sources via `kotlin.sourceSets` DSL. Remove when KSP is updated.
- **Package naming:** namespace changed from `com.sujana.sujana` → `com.sujana`. applicationId = `com.sujana`.

## Resume / progress
- **Status:** ✅ DONE — all compilation verified.
- **Done so far:** All files created and compiled. Both :app assembleDebug and :backend compileKotlin pass.
- **Next:** Open new chat for Stage 1 (Auth & RBAC).

## Status
✅ Complete (pending manual app launch verification on device/emulator).
