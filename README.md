# Lead Magnet CRM

Spring Boot + Gradle API, PostgreSQL, and a Vite/React administrator UI for the lead-magnet workflow.

## Run

```bash
cp .env.example .env
docker compose up -d
./gradlew bootRun
cd frontend && npm install && npm run dev
```

Open `http://localhost:5173`. Flyway applies `src/main/resources/db/migration/V1__initial_schema.sql` before the application starts. The development administrator is `admin` / `admin123`; change it before deployment by seeding a real admin record. Configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `AUTH_SECRET`, and `CORS_ORIGIN` in production.

The local PostgreSQL container exposes port `5433` to avoid conflicts with a locally installed PostgreSQL server.
During development Vite proxies `/api`, `/public`, and `/r` to the Spring API, so browser requests remain same-origin.
The default local configuration creates a demo template, campaign, links, visits, and leads. Set `SEED_DEMO_DATA=false` outside local development.

## Decisions

- Uploaded forms are a single `.html` file and must contain a `<form>`.
- Ready-to-upload sample forms are in `templates/wellness-lead.html`, `templates/beauty-lead.html`, and `templates/career-lead.html`. Each submission is recorded as an application, so the selected campaign's conversion rate updates automatically.
- Public forms run in a sandboxed iframe without scripts, cookies, admin DOM access, or administrator API access. The server injects the public submission target.
- An anonymous, HttpOnly cookie identifies visitors. A visitor may submit once per distribution link.
- The conversion rate is `applications / unique visitors * 100`.
- Instagram, X, YouTube, and Threads are URL attribution channels only; no channel platform APIs are used.

## Feature layout

Backend packages are separated by `auth`, `form`, `campaign`, `distribution`, `tracking`, and `lead`; shared HTTP configuration remains in `config` and `api`. Frontend UI is in `frontend/src/features/{auth,forms,campaigns,distribution,analytics}`, with HTTP access in `frontend/src/shared`.

API paths are documented in [`docs/api.md`](docs/api.md); key architectural decisions are in [`docs/adr/0001-core-design-decisions.md`](docs/adr/0001-core-design-decisions.md).
