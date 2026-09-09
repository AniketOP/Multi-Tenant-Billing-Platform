# Multi-Tenant Housing Society Billing Platform

A full-stack billing and invoicing system for housing societies — one backend serving many independent societies, each with its own owners, units, invoices, and payments, fully isolated from one another.

Built from scratch as a first backend project, staged deliberately: domain modeling → CRUD → auth → tenant isolation → billing logic → async processing → automation → a working frontend.

## What it does

- Each **tenant** (housing society) manages its own **units**, **owners**, **billing profiles** (rate plans), **invoices**, and **payments** — completely walled off from every other tenant.
- Invoices are generated automatically on each tenant's configured billing day, carrying forward the previous month's balance.
- Payments apply against invoices and update balances and status (`PENDING` → `PARTIAL` → `PAID`) automatically.
- Overdue invoices accrue a late fee — percentage or fixed, configurable per tenant.
- Invoice generation runs asynchronously via Kafka rather than blocking the scheduler thread.

## Architecture
                ┌─────────────┐
                │   Browser   │
                │ (React SPA) │
                └──────┬──────┘
                       │ REST + JWT
                ┌──────▼──────┐
                │ Spring Boot │
                │   Backend   │
                └──┬───────┬──┘
                   │       │
          ┌────────▼─┐   ┌─▼────────┐
          │ Couchbase│   │  Kafka   │
          │ (data)   │   │ (events) │
          └──────────┘   └──────────┘

**Tenant isolation** is enforced at three layers, not one:
1. **JWT claims** — every token embeds the user's `tenantId` and `role`.
2. **Request-level guard** — a `HandlerInterceptor` compares the `tenantId` in the URL path against the token's `tenantId` on every `/tenants/{tenantId}/**` route; mismatch → `403`.
3. **Per-record ownership check** — every service method that fetches a record by ID re-verifies that record's own `tenantId` matches, closing the gap where a URL-level match alone wouldn't stop a lookup of another tenant's data by ID.

**Invoice generation flow:**
Scheduler (daily cron)
→ finds tenants whose billing day is today
→ publishes a TenantInvoiceDueEvent per active unit to Kafka
→ a separate consumer picks up each event
→ generates the invoice (pulls last balance, current charges, computes total)

Decoupling generation from the scheduler this way means a tenant with many units doesn't block invoice generation for every other tenant.

## Tech stack

**Backend:** Java 21, Spring Boot 4.1.1, Couchbase, Kafka, JWT (jjwt), BCrypt
**Frontend:** React, TypeScript, Vite, React Router, Axios
**Infra:** Docker Compose (Couchbase, Kafka, Zookeeper)

## Getting started

### Prerequisites
- Java 21
- Node.js + npm
- Docker Desktop

### 1. Start the infrastructure
```bash
docker compose up -d
```
This starts Couchbase, Kafka, and Zookeeper.

On first run, open `http://localhost:8091`, set up a new cluster, create a bucket named `billing`, and run this once in the Couchbase Query workbench:
```sql
CREATE PRIMARY INDEX ON `billing`;
```

### 2. Configure environment variables
The backend reads these at startup — set them in your IDE's run configuration or your shell environment:
COUCHBASE_USERNAME=<your couchbase admin username>
COUCHBASE_PASSWORD=<your couchbase admin password>
JWT_SECRET=<a long random string, used to sign JWTs>


### 3. Run the backend
```bash
cd billing
mvn spring-boot:run
```
Confirm it's healthy at `http://localhost:8080/actuator/health` — look for `"couchbase": {"status": "UP"}`.

### 4. Run the frontend
```bash
cd billing-ui
npm install
npm run dev
```
Visit `http://localhost:5173`.

### 5. Create your first tenant and user
```bash
# Create a tenant
curl -X POST http://localhost:8080/tenants \
  -H "Content-Type: application/json" \
  -d '{"name": "Green Valley Society", "billingDay": 1, "lateFeeType": "PERCENTAGE", "lateFeeValue": 1.5}'

# Sign up an admin user for that tenant (use the tenant id returned above)
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"tenantId": "<tenant-id>", "username": "admin", "password": "yourpassword", "role": "ADMIN"}'
```
Log in through the frontend with those credentials.

## Key design decisions

- **Tenant creation is not self-service.** A housing society isn't something a stranger signs up for — tenants and their initial admin users are created via API/admin action, not a public signup form.
- **PATCH endpoints require every field.** They currently do a full overwrite rather than a partial merge — a deliberate simplicity tradeoff, documented so it isn't a surprise.
- **IDs are human-legible, prefixed strings** (`tenant::<uuid>`, `unit::<uuid>`) rather than raw UUIDs — makes debugging and manual Couchbase queries far easier.

## Known limitations / not yet built

- No automated test suite yet.
- No pagination — fine at current scale, would need addressing before large datasets.
- No UI for editing/deleting owners and profiles (create/read only).
- No Google/OAuth2 login (would require mapping an external identity to an existing tenant-scoped user).

## Project structure
Billing-Platform/
├── billing/ Spring Boot backend
│ └── src/main/java/com/Aniket/billing/
│ ├── model/ Couchbase documents
│ ├── repository/ Data access
│ ├── service/ Business logic
│ ├── controller/ REST endpoints
│ ├── security/ JWT filter, tenant guard
│ ├── event/ Kafka event + listener
│ ├── scheduler/ Cron jobs
│ ├── exception/ Global error handling
│ └── config/ CORS, web config
├── billing-ui/ React + TypeScript frontend
└── docker-compose.yml Couchbase + Kafka, local dev

<img width="1919" height="918" alt="image" src="https://github.com/user-attachments/assets/cd77f01f-5425-4242-91cc-84d3b55bacca" />
<img width="1919" height="919" alt="image" src="https://github.com/user-attachments/assets/6565ed81-009a-41a2-8ed2-4d816f01c19f" />



