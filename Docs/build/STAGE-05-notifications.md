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
- [ ] Backend: `install(WebSockets)` in Ktor plugins; `ws("/ws")` route with Firebase token
      validation on connect; `ConnectionManager` singleton (`ConcurrentHashMap<userId, session>`).
- [ ] Backend: `WsEventBroadcaster` — called from `RequestService` + `AssignmentService` after
      every state transition; pushes JSON to affected sessions.
- [ ] `:shared`: `WsEventType` enum + `WsEventDto(event, entityId, status)`.
- [ ] Android `WebSocketManager` (`@Singleton` via Hilt): OkHttp WS, Firebase-token auth header,
      `SharedFlow<WsEvent>`, exponential back-off reconnect (1 s → 2 s → 4 s … cap 30 s).
- [ ] Wire `WebSocketManager` into `RequestDetailViewModel`, `MyRequestsViewModel`,
      `RiderTasksViewModel`: on matching `WsEvent`, call `silentRefresh()`. Keep 10 s poll as fallback.
- [ ] Open WS on login, close on logout (hook into auth flow).

### FCM + in-app notification center
- [ ] `:shared`: `NotificationCategory` enum + `NotificationDto`, `NotificationPrefDto`,
      `RegisterTokenRequest`.
- [ ] Flyway `V5__notifications.sql`: `notifications(id, user_id, category, title, body,
      deeplink, data_json, read_at null, created_at)`; `notification_prefs(user_id, category,
      muted)`; `device_tokens(user_id, token, platform, updated_at)`.
- [ ] Backend `NotificationService` + emit calls from request/assignment services (a central
      `notify(event)` so feature services stay clean).
- [ ] Backend endpoints: `POST /devices/token`, `GET /notifications` (paged),
      `POST /notifications/{id}/read`, `POST /notifications/read-all`,
      `GET /notification-prefs`, `PUT /notification-prefs`.
- [ ] Recipient resolution by role/tenant + preference filtering; FCM send (Admin SDK) with
      deep-link payload + persist in-app row in the same flow.
- [ ] App: FCM service (token refresh → register; onMessage → notify + deep-link), notification
      channels, POST_NOTIFICATIONS permission (Android 13+).
- [ ] App Compose: `NotificationCenterScreen` (paged, read/unread, badge), `NotificationPrefsScreen`.
- [ ] Deep-link routing: tapping a notification opens the right screen (request/assignment/etc.).

## Data-model changes (Postgres)
- `V5__notifications.sql` — `notifications`, `notification_prefs`, `device_tokens` (above).

## API endpoints
- `ws://…/ws` — WebSocket connection (Firebase token auth, push-only from server).
- `POST /devices/token` · `GET /notifications` · `POST /notifications/{id}/read` ·
  `POST /notifications/read-all` · `GET /notification-prefs` · `PUT /notification-prefs`.

## Acceptance criteria
- [ ] Status change on one device appears on all other open devices within ~1 s (WebSocket).
- [ ] Closing the app and triggering a status change delivers an FCM push within ~5 s.
- [ ] A status change fires the correct notifications to the correct roles (push + in-app row).
- [ ] Notification center shows paged history with working read/unread + badge count.
- [ ] Muting a category stops that category's notifications for that user.
- [ ] Tapping a push deep-links to the correct screen (foreground & background).
- [ ] Token registration survives reinstall/refresh; stale tokens handled.
- [ ] WS reconnects automatically after network loss without manual app restart.

## Handoff notes (fill when done)
- Central event→recipients mapping location: _____
- Notification channels/categories created: _____
- Deep-link scheme/route table: _____

## Resume / progress
_Mid-stage handoff notes. Update before ending an unfinished chat (see new-chat protocol)._
- **Resume here (next action):** _stage not started_
- **Done so far:** —
- **Gotchas / half-finished / uncommitted:** —

## Status
⬜ Not started.
