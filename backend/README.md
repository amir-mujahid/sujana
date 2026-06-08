# I-Sujana Backend

Ktor + Exposed + Flyway API server. Verifies Firebase ID tokens; uses Postgres as the system of record.

## Prerequisites

- JDK 17+
- PostgreSQL 15+ (local) **or** a Neon dev branch
- A Firebase project with a service account key

## Environment variables

| Variable | Description | Example |
|---|---|---|
| `DATABASE_URL` | JDBC URL | `jdbc:postgresql://localhost:5432/sujana_dev` |
| `DATABASE_USER` | Postgres username | `postgres` |
| `DATABASE_PASSWORD` | Postgres password | `secret` |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Full contents of the Firebase service account JSON key | `{ "type": "service_account", ... }` |
| `PORT` | HTTP port (default `8080`) | `8080` |

## Local setup

1. Create a local Postgres database:
   ```sql
   CREATE DATABASE sujana_dev;
   ```

2. Set environment variables (add to your shell profile or a `.env` file):
   ```bash
   export DATABASE_URL="jdbc:postgresql://localhost:5432/sujana_dev"
   export DATABASE_USER="postgres"
   export DATABASE_PASSWORD="your_password"
   export FIREBASE_SERVICE_ACCOUNT_JSON="$(cat path/to/service-account.json)"
   ```

3. Run the backend (Flyway migrations apply automatically on startup):
   ```bash
   ./gradlew :backend:run
   ```

4. Test: `curl http://localhost:8080/health`
   Expected: `{"status":"ok","dbConnected":true,"time":"..."}`

## Android emulator → backend

When running on the Android emulator, use `10.0.2.2` to reach the host machine's localhost:
```
http://10.0.2.2:8080
```
This is already set as `BASE_URL` for the `debug` build type in `:app`.

## Neon dev branch (alternative to local Postgres)

Create a dev branch on https://neon.tech, copy the connection string, and set:
```bash
export DATABASE_URL="jdbc:postgresql://<neon-host>/<db>?sslmode=require"
export DATABASE_USER="<neon-user>"
export DATABASE_PASSWORD="<neon-password>"
```
