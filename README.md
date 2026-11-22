# Atlant

Three independent projects for Atlant, a water filtration equipment business.
Each has its own dependencies and commands — there is no root build system.

| Project | Path | Stack |
|---|---|---|
| Backend API | `atlant-backend-main/` | Spring Boot 3.5.6 (Java 17) |
| Public site | `atlant-frontend-main/` | Next.js 15 |
| Admin dashboard | `atlant_dashboard-main/` | Next.js 16 |

Both frontends call the backend. The public site runs on `:3000`, the
dashboard on `:3001` (fixed in `package.json` so the two don't collide), and
the backend on `:8080`.

## Prerequisites

- **Java 17** — `java -version` should report 17.x. If nothing is installed,
  get a JDK from [Adoptium](https://adoptium.net/temurin/releases/?version=17)
  or `brew install openjdk@17` (on Apple Silicon; Homebrew no longer ships
  bottles for Intel Macs, so prefer the Adoptium tarball there).
- **Node.js 20+** and npm.
- **Docker** (for local Postgres) — or a Postgres 16 instance you point the
  backend at directly.

## First-time setup

1. **Database** — from `atlant-backend-main/`:
   ```bash
   docker compose up -d
   ```
   This starts Postgres on `localhost:5433` with db/user `atlant`.

2. **Backend** — `cd atlant-backend-main`, copy `.env.example` to `.env`,
   fill in `JWT_SECRET_KEY` (any long random string for local dev) and the
   AWS S3 credentials (ask a teammate for the shared bucket — without these,
   image upload features will fail, everything else works). Then:
   ```bash
   ./mvnw spring-boot:run
   ```
   Swagger UI: http://localhost:8080/swagger-ui.html

3. **Public site** — `cd atlant-frontend-main`, copy `.env.example` to
   `.env.local`, then:
   ```bash
   npm install
   npm run dev
   ```
   http://localhost:3000

4. **Admin dashboard** — `cd atlant_dashboard-main`, copy `.env.example` to
   `.env.local`, then:
   ```bash
   npm install
   npm run dev
   ```
   http://localhost:3001

## Notes

- Telegram notifications on contact-form submissions are optional locally —
  if `TELEGRAM_BOT_TOKEN`/`TELEGRAM_CHAT_ID` are left blank in the backend
  `.env`, the backend just logs a warning and continues. Once the backend is
  running, a bot/chat can also be configured from the dashboard's settings
  page, which takes priority over the env vars.
- The public frontend has both `package-lock.json` and `yarn.lock` checked
  in. Use npm (`npm install`) to avoid dependency drift between the two.
- See each project's own `CLAUDE.md` section (root `CLAUDE.md`) for
  architecture details, or `atlant_dashboard-main/DProjectsatlant_dashboardapi-spec.json`
  for the backend's OpenAPI contract.
