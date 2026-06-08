# I-Sujana — Project State (READ THIS FIRST)

> **This is the entry point for every chat.** It is intentionally small. Read it fully,
> then read `CONVENTIONS.md`, then the **one** stage file you are working on (and
> `DESIGN-SYSTEM.md` if that stage touches UI). Do **not** read `Docs/documentation.md`
> or other stage files — they waste tokens and break the one-stage-per-chat rule.

---

## What this project is

**I-Sujana** — a Smart Municipal Waste Collection & Logistics System for schools and
communities in Selangor, Malaysia. Android app + backend API. Multi-tenant, role-based.
Two workflows:

- **Grab-style collection** (core MVP): Contributor requests pickup → Rider collects from
  house → delivers to a school collection center.
- **School → MPS**: School submits waste request → MPS Dispatcher assigns a rider → collected.

Seven roles: Super Admin, MPS Admin, MPS Dispatcher, School Admin, School Staff,
Rider/Driver, Contributor.

The full original vision is in `Docs/documentation.md` — **reference only, do not load it
during normal stage work.** Everything you need to build is distilled into the stage files.

---

## Locked decisions (do not relitigate without the user)

| Area | Decision |
|------|----------|
| **Backend model** | **Hybrid.** Neon **Postgres = system of record** (relational logistics + analytics). Firebase = **Auth + Realtime DB (live tracking) + FCM + Crashlytics/Analytics** (no Storage — not free). |
| **Backend API** | **Ktor + Exposed** (Kotlin) + **Flyway** migrations. Verifies Firebase ID tokens via **Firebase Admin SDK**, then authorizes against Postgres. |
| **Hosting** | **Cloud Run** free tier (target); fallback **Render/Railway**. Develop against **local Postgres + Neon dev branch** until Stage 10. |
| **Maps** | **Google Maps** — Maps Compose (`com.google.maps.android:maps-compose`) + Directions/Routes API. |
| **MVP strategy** | **Core vertical slice first** (Contributor→Rider→School end-to-end), then breadth. |
| **Live GPS tracking** | **In MVP.** Rider foreground location service → Firebase **Realtime DB** → requester's Google Map. Postgres holds assignment lifecycle; Realtime DB holds ephemeral position. |
| **Build tooling** | **KSP** (never kapt), **Gradle Version Catalogs** (`gradle/libs.versions.toml`), Hilt, Compose + Material 3, Clean Architecture + MVVM. |
| **Design language** | Enterprise **"Trust & Authority"** — minimal/Swiss, Inter font, slate-navy neutrals + emerald accent, full light+dark. See `DESIGN-SYSTEM.md`. **No AI-slop / vibe-coded look.** |
| **Repo shape** | Gradle monorepo: **`:app`** (Android) · **`:backend`** (Ktor JVM) · **`:shared`** (pure-Kotlin DTOs shared by both). |

| **File/image uploads** | **Cloudinary** (free tier available). Firebase Storage is NOT used — requires Blaze plan. |

**Free-tier caveats** (do not block local dev): Cloud Run & Google Maps need a billing account even on free tier.

---

## Repo layout (target after Stage 0)

```
Sujana/
├── app/                      # :app  — Android (Kotlin, Compose, Hilt, Clean Arch)
│   └── src/main/java/com/sujana/...
│       ├── core/             # theme, di, network, common utils
│       ├── data/             # repositories impl, remote (Retrofit), local (Room, DataStore)
│       ├── domain/           # entities, repository interfaces, use cases
│       └── feature/          # per-feature ui + viewmodel (auth, request, dispatch, ...)
├── backend/                  # :backend — Ktor + Exposed + Flyway
│   └── src/main/kotlin/com/sujana/backend/...
│       ├── plugins/          # auth (Firebase Admin), serialization, status pages, DI
│       ├── db/               # Exposed tables, Flyway migrations (resources/db/migration)
│       ├── feature/          # routes + services per domain
│       └── Application.kt
├── shared/                   # :shared — pure-Kotlin DTOs + enums shared by app & backend
│   └── src/main/kotlin/com/sujana/shared/...
├── gradle/libs.versions.toml # version catalog (single source of dependency versions)
├── Docs/
│   ├── documentation.md      # original vision (reference only)
│   └── build/                # THIS continuity system
└── settings.gradle.kts       # includes :app, :backend, :shared
```

> Until Stage 0 runs, the repo is still the stock single-module Android Views template.

---

## Global Stage Checklist

Legend: ⬜ not started · 🔶 in progress · ✅ done

| # | Stage | Status | One-line state |
|---|-------|:------:|----------------|
| 0 | Foundation & Infrastructure | ✅ | Monorepo, Compose, catalog, theme, Firebase deps, Ktor+Postgres skeleton |
| 1 | Auth & RBAC | ✅ | Firebase Auth, 7 roles, custom claims, `/auth/me`, role-gated nav, DataStore session |
| 2 | Core Request System | ⬜ | Contributor pickup requests, `requests` table, list/detail |
| 3 | Dispatch & Assignment | ⬜ | `assignments`, rider inbox, status transitions |
| 4 | Live Tracking & Maps | ⬜ | Rider GPS → Realtime DB → requester map; routing |
| 5 | Notifications (all roles) | ⬜ | FCM + in-app center, `notifications` table, per-role events, prefs |
| 6 | School→MPS Workflow | ⬜ | Second workflow on shared request/assignment infra |
| 7 | Admin & Tenant Management | ⬜ | Tenants/schools/waste-points/users CRUD, audit logs |
| 8 | Analytics Dashboard | ⬜ | Postgres aggregates + charts |
| 9 | Offline · Performance · Security | ⬜ | Room cache, Paging 3, indexing, hardening, R8 |
| 10 | Observability · Testing · Deploy | ⬜ | Crashlytics/Analytics, tests, deploy backend |

**▶ CURRENT STAGE: 2 — Core Request System** (`STAGE-02-core-request.md`)

> When you finish a stage: tick every box in its stage file, fill its **Handoff notes**,
> set its row above to ✅, move the **▶ CURRENT STAGE** pointer to the next stage, then STOP.

Stages 0→4 are strictly sequential. After 4, stages 5/6/7/8 each depend only on 0–4 and may
be reordered by the user; 9 and 10 come last.

---

## New-chat protocol (the contract — follow it exactly)

When a chat starts work on this project:

1. **Read** `Docs/build/00-PROJECT-STATE.md` (this file) + `Docs/build/CONVENTIONS.md` +
   the **▶ CURRENT STAGE** file. Add `Docs/build/DESIGN-SYSTEM.md` if the stage touches UI.
   Optionally `ARCHITECTURE.md` if you need a flow/diagram. **Nothing else** — never load
   `documentation.md` or other stage files.
2. **Check the stage's `Resume / progress` block first.** If it has a "Resume here" note or
   some `- [x]` ticked tasks, the stage is mid-flight — continue from there; do NOT restart
   completed tasks. (Trust ticked boxes; spot-check the named files only if something looks off.)
3. **Work only the current stage.** Do not start the next stage's scope, even if it seems easy.
4. **Tick boxes as you go, not at the end.** The instant a `- [ ]` task is genuinely done,
   change it to `- [x]`. This is what makes mid-stage resume possible.
5. When the stage's **Acceptance criteria** are met: fill in the stage's **Handoff notes**,
   set its row in the Global Stage Checklist to ✅, advance the **▶ CURRENT STAGE** pointer,
   and update any decision/schema notes that changed. Then **STOP** and tell the user:
   "Stage N complete — start a new chat for Stage N+1."
6. If a decision in the locked table needs to change, ask the user first, then update this
   file so future chats inherit it.

### Ending a chat with the stage NOT finished (mid-stage handoff)

Before you stop for any reason (user leaving, context getting long), do this so the next chat
picks up cleanly:

- Make sure every completed `- [ ]` is now `- [x]`.
- Set the stage's **Status** line to `🔶 In progress` and the Global Stage Checklist row to 🔶.
- Fill the stage's **`Resume / progress`** block with: what's done, what's **next**, and any
  half-finished/uncommitted detail or gotcha the next chat must know (e.g. "started
  `RequestRepository`, mapper not written; backend `POST /requests` returns 500 — unfinished").
- Tell the user plainly that the stage is partway done and to say
  **"Continue I-Sujana — work the current stage"** in the next chat to resume.

> The next chat resumes from the `Resume / progress` block + ticked boxes + the code on disk.
> If nothing was written down, it can only re-derive progress by reading the repo (slower) —
> so always leave the note.
