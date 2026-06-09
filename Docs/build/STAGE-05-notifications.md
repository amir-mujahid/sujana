# STAGE 05 — Notifications (comprehensive, all roles)

**Goal:** A full real-time communication layer serving **every role** — WebSocket foreground
push + FCM background push + an in-app notification center backed by Postgres — driven by
domain events across the request/assignment lifecycle, with per-user preferences and
deep-links to the relevant screen.

**Prerequisites:** Stage 0–4 ✅ (the events that trigger notifications exist).

---

## In scope

### WebSockets — foreground real-time state sync
- Backend: `install(WebSockets)` in Ktor; `ws("/ws")` route — validate Firebase ID token on
  connect (same flow as REST auth), store `userId → DefaultWebSocketSession` in a
  `ConcurrentHashMap`. Clean up on disconnect.
- Backend event broadcasting: after every `RequestService` / `AssignmentService` state change,
  look up affected user sessions and push a small JSON event:
  `{"event":"REQUEST_STATUS_CHANGED","requestId":"…","status":"ASSIGNED"}` etc.
- Android `WebSocketManager` (`@Singleton`): opens one OkHttp WebSocket per login, sends
  Firebase ID token as `Authorization` header, exposes `SharedFlow<WsEvent>`. Reconnects with
  exponential back-off on disconnect.
- ViewModels (`RequestDetailViewModel`, `MyRequestsViewModel`, `RiderTasksViewModel`) collect
  `WsEvent` and call `silentRefresh()` on the relevant resource. The existing 10 s poll stays
  as a silent fallback.

### FCM — background push + in-app center
- Postgres `notifications` + `notification_prefs` tables; `NotificationCategory` enum in `:shared`.
- Device-token registration (FCM) per user; topic + targeted token sends.
- Backend `NotificationService`: on domain events, resolve recipients (by role/tenant +
  preferences), persist in-app rows, send FCM with a deep-link payload.
- In-app **notification center**: paged list, read/unread, mark-read, badge counts.
- Per-user **preferences** screen: mute categories.
- App FCM handler: foreground + background, builds notification, deep-links into the app.
- **Per-role event catalog** wired to existing events (see below).

### Per-role event catalog (minimum)
- **Contributor:** request received, assigned, rider en route, collected, completed, cancelled.
- **Rider:** new assignment, assignment changed/cancelled, pickup detail changed.
- **School Admin / Staff:** request status changes, incoming collection scheduled, schedule changes.
- **MPS Dispatcher:** new incoming request, SLA/overdue alert, rider issue/decline.
- **MPS Admin:** operational summary, escalations.
- **Super Admin:** system/tenant-level alerts.

## Out of scope (defer)
- SLA computation depth & analytics → **Stage 8** (here, a simple overdue check is enough to
  fire the alert).
- Email/SMS channels → not planned unless requested.

---

## Task checklist

### WebSockets
- [x] Backend: `install(WebSockets)` in Ktor plugins; `ws("/ws")` route with Firebase token
      validation on connect; `ConnectionManager` singleton (`ConcurrentHashMap<userId, session>`).
- [x] Backend: `WsEventBroadcaster` — called from `RequestService` + `AssignmentService` after
      every state transition; pushes JSON to affected sessions.
- [x] `:shared`: `WsEventType` enum + `WsEventDto(event, entityId, status)`.
- [x] Android `WebSocketManager` (`@Singleton` via Hilt): OkHttp WS, Firebase-token auth header,
      `SharedFlow<WsEvent>`, exponential back-off reconnect (1 s → 2 s → 4 s … cap 30 s).
- [x] Wire `WebSocketManager` into `RequestDetailViewModel`, `MyRequestsViewModel`,
      `RiderTasksViewModel`: on matching `WsEvent`, call `silentRefresh()`. Keep 10 s poll as fallback.
- [x] Open WS on login, close on logout (hook into auth flow via `MainViewModel`).

### FCM + in-app notification center
- [x] `:shared`: `NotificationCategory` enum + `NotificationDto`, `NotificationPrefDto`,
      `RegisterTokenRequest`.
- [x] Flyway `V5__notifications.sql`: `notifications(id, user_id, category, title, body,
      deeplink, data_json, read_at null, created_at)`; `notification_prefs(user_id, category,
      muted)`; `device_tokens(user_id, token, platform, updated_at)`.
- [x] Backend `NotificationService` + emit calls from request/assignment services (a central
      `notify(event)` so feature services stay clean).
- [x] Backend endpoints: `POST /devices/token`, `GET /notifications` (paged),
      `POST /notifications/{id}/read`, `POST /notifications/read-all`,
      `GET /notification-prefs`, `PUT /notification-prefs`.
- [x] Recipient resolution by role/tenant + preference filtering; FCM send (Admin SDK) with
      deep-link payload + persist in-app row in the same flow.
- [x] App: FCM service (token refresh → register; onMessage → notify + deep-link), notification
      channels, POST_NOTIFICATIONS permission (Android 13+).
- [x] App Compose: `NotificationCenterScreen` (paged, read/unread, badge), `NotificationPrefsScreen`.
- [x] Deep-link routing: tapping a notification opens the right screen (request/assignment/etc.).

## Data-model changes (Postgres)
- `V5__notifications.sql` — `notifications`, `notification_prefs`, `device_tokens` (above).

## API endpoints
- `ws://…/ws` — WebSocket connection (Firebase token auth, push-only from server).
- `POST /devices/token` · `GET /notifications` · `POST /notifications/{id}/read` ·
  `POST /notifications/read-all` · `GET /notification-prefs` · `PUT /notification-prefs`.

## Acceptance criteria
- [x] Status change on one device appears on all other open devices within ~1 s (WebSocket).
- [x] Closing the app and triggering a status change delivers an FCM push within ~5 s.
- [x] A status change fires the correct notifications to the correct roles (push + in-app row).
- [x] Notification center shows paged history with working read/unread + badge count.
- [x] Muting a category stops that category's notifications for that user.
- [x] Tapping a push deep-links to the correct screen (foreground & background).
- [x] Token registration survives reinstall/refresh; stale tokens handled.
- [x] WS reconnects automatically after network loss without manual app restart.

## Handoff notes
- **Central event→recipients mapping location:** `NotificationService.kt` — `onRequestCreated`,
  `onRequestStatusChanged`, `onNewAssignment`, `onAssignmentStatusChanged`,
  `notifyDispatchersNewRequest`. All called from `RequestRoutes.kt` / `AssignmentRoutes.kt`
  after the service call returns (outside the Exposed transaction).
- **Notification channels/categories created:** `SujanaApp.NOTIFICATIONS_CHANNEL_ID =
  "sujana_notifications"` (IMPORTANCE_DEFAULT). Tracking channel unchanged.
  Categories in `NotificationCategory` enum: REQUEST_UPDATE, ASSIGNMENT_UPDATE,
  SCHOOL_COLLECTION, DISPATCH_ALERT, ADMIN_SUMMARY, SYSTEM.
- **Deep-link scheme/route table:** `sujana://request/{id}` → REQUEST_DETAIL composable;
  `sujana://assignment/{id}` → TASK_DETAIL composable; `sujana://dispatch` → DISPATCH_QUEUE;
  `sujana://notifications` → NOTIFICATIONS. Registered via `navDeepLink` in NavGraph +
  `handleSujanaDeepLink()` helper for in-app navigation from NotificationCenter.
- **WS URL derivation:** `NotificationModule.provideWsUrl()` — replaces `http://`→`ws://`,
  `https://`→`wss://`, appends `/ws`. Bare OkHttpClient (no auth interceptor) used for WS;
  auth is sent as `Authorization: Bearer <token>` header on the upgrade request.
- **Dev mode WS auth:** Backend `WebSocketRoutes.resolveUserDbId()` maps Firebase uid
  `"dev-uid"` to the Postgres user row when no Firebase app is initialised.

## Resume / progress
_Stage complete._

## Status
✅ Done.
