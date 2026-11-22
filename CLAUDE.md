# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository overview

This directory contains three independent projects for **Atlant**, a water filtration equipment business. There is no root-level build system tying them together — each has its own dependencies, commands, and deploy target.

| Project | Path | Stack | Role |
|---|---|---|---|
| Backend API | `atlant-backend-main/` | Spring Boot 3.5.6 (Java 17) | REST API, auth, persistence, S3 file storage |
| Public site | `atlant-frontend-main/` | Next.js 15 (App Router) | Public-facing marketing site, i18n (en/ru/uz) |
| Admin dashboard | `atlant_dashboard-main/` | Next.js 16 (App Router) | Internal CRUD panel for managing site content |

The two frontends both consume the same backend, deployed at `https://atlant-backend-production.up.railway.app/api/v1`. The public site reads only `*/public/**` endpoints; the dashboard uses the `*/admin/**` endpoints and is where products/team/licenses/gallery/carousel content is actually authored. `atlant_dashboard-main/DProjectsatlant_dashboardapi-spec.json` is the OpenAPI contract for the backend — consult it instead of guessing endpoint shapes.

---

## atlant-backend-main (Spring Boot)

### Commands
```bash
./mvnw spring-boot:run        # run locally
./mvnw clean package          # build jar (tests run by default)
./mvnw clean package -DskipTests
./mvnw test                   # run all tests
./mvnw test -Dtest=ClassName  # run a single test class
```
Swagger UI is available at `/swagger-ui.html` when running.

Required env vars (no `.env` committed; loaded via `java-dotenv`): `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_BUCKET_NAME`, `JWT_SECRET_KEY`.

### Architecture
Standard layered monolith: `controller` → `service` → `repositories` → `entities`, with manual (non-MapStruct) mappers in `mapper/` converting entities to DTOs. Package root: `src/main/java/sayyeed/dev/atlantbackend/`.

- **`controller/`** — one controller per resource (Product, TeamMember, License, Gallery, Carousel, Attach, Auth) plus a global `ExceptionHandlerController`.
- **`config/security/`** — `SpringSecurityConfig` defines the open/public endpoint allowlist (`openApiList`) and registers `JwtAuthenticationFilter` ahead of `UsernamePasswordAuthenticationFilter`. Role checks are `hasRole("ADMIN")` per path pattern, not method-level annotations — check this file before adding a new endpoint.
- **Auth model**: stateless JWT (jjwt 0.12.6), 7-day expiry, claims carry `email` + `role` (`utils/JwtUtil`). Passwords hashed with BCrypt. **There is no open self-registration** — `/api/v1/auth/register` itself requires ADMIN role, so new accounts are created by an existing admin via the dashboard.
- **Database**: PostgreSQL, schema managed by `spring.jpa.hibernate.ddl-auto=update` — **no Flyway/Liquibase**, so there are no migration files to update when changing an entity; Hibernate auto-DDL handles it.
- **File storage**: AWS S3 via `service/aws/IS3Service` / `S3ServiceImpl`, used for product images, gallery photos, licenses, and team photos, fronted by `AttachController`/`AttachEntity` as a generic attachment abstraction (`/attach/open/{fileId}`, `/attach/download/{fileId}`).
- **Multi-language fields**: content entities (Product, TeamMember, License, Carousel) carry EN/RU/UZ variants of text fields — mirrors the `MultiLangText` pattern used in both frontends.
- **CORS is wide open** (`allowedOriginPatterns("*")`) and CSRF is disabled, consistent with a stateless public/admin JWT API — be aware of this when touching `SpringSecurityConfig`.
- There's an `/api/v1/ai/**` path pre-permitted in security config with no corresponding controller yet — don't assume it's wired up.

---

## atlant-frontend-main (public site)

### Commands
```bash
npm run dev     # next dev --turbopack
npm run build   # next build --turbopack
npm run start
npm run lint
```
No test script/framework is configured in this project.

Both `yarn.lock` and `package-lock.json` exist; pick one going forward to avoid drift (lean npm, since the dashboard and README default to it).

### Architecture
Next.js App Router with locale-prefixed routing via `next-intl`:
- All pages live under `app/[locale]/...` (e.g. `app/[locale]/product/[id]/`); locales are `en`, `ru`, `uz` with `ru` as default (`i18n/routing.ts`). `middleware.ts` handles locale negotiation/redirects.
- Translation catalogs: `messages/{en,ru,uz}.json`.
- Shared components are split by role: `components/layout/` (Navbar, Footer), `components/features/` (Carousel, Gallery, Catalogproduct, etc.), `components/shared/` (ProductCard, LanguageSwitcher), `components/ui/` (shadcn, "new-york" style). Feature-local components live colocated under each route, e.g. `app/[locale]/about/components/`.
- **Data fetching**: `lib/api/*.ts` modules (products, carousel, gallery, licenses, team) call the backend's `*/public/**` endpoints directly with `fetch`, passing `Accept-Language` derived from the active locale and `cache: 'no-store'`. (`swr`/`axios` are listed as deps but the actual fetch modules use plain `fetch`.)
- `data/` centralizes hardcoded content: `metadata.ts` (per-page SEO/OG metadata), `navigation.tsx` (nav links). This centralization pattern (and the `components/{layout,features,shared,ui}` split) came out of a past refactor documented in `STRUCTURE_IMPROVEMENTS.md` — note that doc predates the `[locale]` restructuring, so some paths it references are stale.
- No form library and no global state manager are in use — state is local/component-level.
- Path alias: `@/*` → project root (not `src/`).

---

## atlant_dashboard-main (admin dashboard)

### Commands
```bash
npm run dev     # next dev -p 3001 (fixed port so it doesn't collide with the public frontend's 3000)
npm run build
npm run start   # next start -p 3001
npm run lint
```
No test script/framework is configured in this project.

### Architecture
Next.js App Router using route groups to separate auth state:
- `app/(auth)/{login,register}/page.tsx` — unauthenticated.
- `app/(dashboard)/dashboard/{products,team,licenses,gallery,carousel,settings}/` — authenticated, gated by `components/auth/ProtectedRoute.tsx`. Several resources have `[id]` dynamic edit routes (e.g. `products/[id]`, `team/[id]`).
- **Auth**: `lib/store/auth.ts` is a Zustand store wrapping `lib/api/auth.ts`; JWT is stored in localStorage, auto-injected by an Axios interceptor in `lib/api/client.ts`, with auto-redirect to `/login` on 401.
- **Data fetching**: `hooks/use-api.ts` exposes per-resource SWR hooks (`useProducts`, etc.) over the `lib/api/*` Axios modules, with shared config (no revalidate-on-focus, 5s dedupe, no retry).
- **Forms**: React Hook Form + Zod, with reusable multi-language fields `components/forms/MultiLangInput.tsx` / `MultiLangTextarea.tsx` — use these for any new field that has EN/RU/UZ variants, matching the backend's multi-language entity shape.
- **Types**: all shared DTOs/enums live in a single `types/index.ts` (`ProductCategory`, `Language`, `MultiLangText`, per-resource request/response types, `ApiResponse<T>`/`PaginatedResponse<T>`) — mirror the backend DTOs here when the API contract changes.
- Charts via Recharts (`components/dashboard/StatsCard.tsx` + dashboard home), tables via `@tanstack/react-table`.
- Path alias: `@/*` → project root; `hooks` alias is configured in `components.json` and does map to a real `hooks/` directory here (unlike the public frontend).

### Cross-cutting conventions
- Both frontends talk to the same backend contract — if you add/change a field on a content entity (Product, TeamMember, License, Carousel, Gallery), update the backend entity/DTO/mapper, the OpenAPI spec file, and the corresponding types in both `atlant-frontend-main/lib/api/*` and `atlant_dashboard-main/types/index.ts` + `lib/api/*`.
- Multi-language (EN/RU/UZ) is a first-class concept across all three projects — new user-facing text fields should follow the existing `MultiLangText`/per-locale pattern rather than being hardcoded to one language.
