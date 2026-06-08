# I-Sujana — Architecture (reference)

Read when you need a flow or the data model. Not required for every stage.

---

## System shape (hybrid)

```
                         ┌────────────────────────────────────────┐
                         │              Android app (:app)          │
                         │  Compose UI → ViewModel → UseCase →      │
                         │  Repository → (Retrofit | Firebase SDK)  │
                         └───────────────┬───────────────┬──────────┘
             Bearer ID token (REST)      │               │  SDKs (realtime/auth/fcm)
                                         ▼               ▼
                  ┌──────────────────────────┐   ┌───────────────────────────────┐
                  │   Backend API (:backend)  │   │           Firebase            │
                  │   Ktor + Exposed          │   │  Auth · Realtime DB · FCM ·   │
                  │   verifies Firebase token │   │  Crashlytics · Analytics      │
                  │   via Firebase Admin SDK  │   └───────────────────────────────┘
                  └───────────┬───────────────┘
                              ▼
                  ┌──────────────────────────┐
                  │   Neon Postgres (truth)   │
                  │   users, tenants, schools │
                  │   requests, assignments,  │
                  │   notifications, audit     │
                  └──────────────────────────┘
```

**Division of labor**
- **Postgres = system of record:** all durable relational data + analytics queries.
- **Firebase Auth = identity** (issues ID tokens; email/password).
- **Firebase Realtime DB = ephemeral live position** for rider tracking (low-latency streaming).
- **FCM = push**; **Cloudinary = image/file uploads** (not Firebase Storage — free tier limit); **Crashlytics/Analytics = observability**.
- **`:shared`** holds wire DTOs + enums so client and server can't drift.

---

## Auth & RBAC flow

```
1. App: Firebase Auth sign-in (email/pw) → ID token
2. App: GET /auth/me  (Authorization: Bearer <idToken>)
3. Backend: verify token (Firebase Admin) → uid
4. Backend: upsert/read user row in Postgres → { role, tenantId, profile }
5. Backend: set Firebase custom claims {role, tenantId} (mirror for client + RTDB rules)
6. App: route to role home; cache session (DataStore)
7. Every later API call carries the ID token; backend re-verifies + authorizes by role/tenant
```

- **Postgres is authoritative** for role/tenant; custom claims are a cache for fast gating.
- RBAC enforced **server-side on every mutating endpoint** (role + tenant scope).

---

## Live tracking flow (Stage 4)

```
Rider app (foreground location service)
   └─ writes {lat,lng,heading,ts} → RTDB: tracking/{assignmentId}
Requester app
   └─ subscribes RTDB: tracking/{assignmentId} → moves Google Map marker
Assignment lifecycle (PENDING→ASSIGNED→COLLECTED→DELIVERED→COMPLETED)
   └─ lives in Postgres via backend; RTDB node cleared/archived on COMPLETED
Routing: Directions/Routes API → polyline drawn in accent color
```
Why RTDB not Postgres for position: high-frequency moving points need streaming, not polling.

---

## Notification fan-out flow (Stage 5)

```
Domain event (e.g. assignment created, status changed, SLA overdue)
   └─ backend NotificationService:
        1. resolve recipients by role/tenant + per-user preferences
        2. write rows → Postgres `notifications` (in-app center: read/unread)
        3. send FCM (targeted token(s) or topic) with deep-link payload
App
   └─ in-app notification center reads `notifications` via API (paged)
   └─ FCM handler shows push + deep-links to the relevant screen
```
Per-role event catalog and per-user mute/category preferences are defined in `STAGE-05`.

---

## Data model overview (Postgres, evolves per stage)

Created progressively via Flyway migrations. Core tables:

- **users** — id(uuid), firebaseUid, name, email, role(enum), tenantId(fk, nullable for
  super_admin/contributor), phone, createdAt. *(Stage 1)*
- **tenants** — id, name (an MPS council), createdAt. *(Stage 1 minimal, Stage 7 full)*
- **schools** — id, tenantId(fk), name, location(lat/lng + address). *(Stage 1 minimal, 7 full)*
- **waste_points** — id, schoolId/tenantId, location, type. *(Stage 7)*
- **requests** — id, type(CONTRIBUTOR|SCHOOL), requesterId, status, pickupLocation,
  dropoffSchoolId, notes, createdAt. *(Stage 2; SCHOOL flow Stage 6)*
- **assignments** — id, requestId(fk), riderId(fk), dispatcherId, status, timestamps. *(Stage 3)*
- **notifications** — id, userId, type, title, body, deeplink, readAt, createdAt. *(Stage 5)*
- **notification_prefs** — userId, category, muted. *(Stage 5)*
- **audit_logs** — id, actorId, action, entity, entityId, timestamp. *(Stage 7)*

Shared enums (`:shared`): `Role`, `RequestType`, `RequestStatus`, `AssignmentStatus`,
`NotificationCategory`.

Firebase paths: `tracking/{assignmentId}` (RTDB). Images/files stored in **Cloudinary** (public_id convention: `sujana/requests/{requestId}/{filename}`).

---

## Backend deploy (Stage 10)

Containerized Ktor → **Cloud Run** (free tier; needs billing account) or **Render/Railway**
fallback. Postgres = **Neon** (free tier; dev branch used throughout development). Secrets via
env vars (DB URL, Firebase service-account JSON, Maps server key).
