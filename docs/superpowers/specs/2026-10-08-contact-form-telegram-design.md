# Contact Form → Telegram Notification — Design

Status: Approved (2026-10-08)

## Purpose

The contact form at `/{locale}/contact` (name, phone, message) currently fakes
success with a client-side `setTimeout` — nothing is sent anywhere. The
marketing team needs real leads from this form delivered to a Telegram group
in real time, and persisted so no lead is lost even if Telegram delivery
fails.

**Success criteria:**
- Submitting the form sends a formatted message to the marketing team's
  Telegram group.
- The submission is durably saved in Postgres regardless of Telegram's
  availability.
- The public endpoint is reasonably protected against spam/abuse.
- A failure that loses the user's data (not just a Telegram hiccup) is shown
  to the user so they can retry.

## Chosen approach

**New endpoint on the existing Spring Boot backend** (`atlant-backend-main`),
following the same architecture as every other public-facing feature in this
app (products, team, gallery, etc: `controller` → `service` → `repository` →
`entity`).

Two alternatives were considered and rejected:
- **Next.js API route calling Telegram directly** — would keep the contact
  form alive even if the Spring backend is down, but splits business logic
  across two runtimes (this codebase's convention is all backend logic lives
  in Spring), and the chosen rate-limiting requirement (in-memory per-IP
  counters) is unreliable on serverless functions, which don't guarantee a
  long-lived process between invocations.
- **Third-party form/automation service** (Zapier, Make.com) — zero custom
  code, but adds a paid third-party dependency and less control over the
  exact message format.

## Architecture & data flow

1. User fills the form on `/{locale}/contact` (name, phone, message) →
   frontend sends `POST /api/v1/contact/public`.
2. Backend validates the payload (`@NotBlank`, length limits, basic phone
   pattern) and checks the request's IP against an in-memory rate limiter
   (~5 requests/minute/IP).
3. Backend saves a `ContactRequestEntity` row (name, phone, message, locale,
   source page, created_at). This happens **before** attempting Telegram
   delivery, so the lead is durable even if Telegram is unreachable.
4. Backend calls the Telegram Bot API's `sendMessage` with the formatted
   message (below). This call is best-effort: failure is logged server-side
   but does not fail the request, since the lead is already safely stored.
5. Response to frontend:
   - DB save succeeded → `200 OK` (regardless of Telegram outcome).
   - DB save failed, or validation/rate-limit rejected → error response;
     frontend shows a retryable error and preserves the user's entered data.

### Message format (Telegram)

```
🆕 Новая заявка с сайта

👤 Имя: {name}
📞 Телефон: {phone}
💬 Сообщение: {message}
🌐 Страница: {locale}/contact
🕒 Время: {yyyy-MM-dd HH:mm}
```

## Components

### Backend (`atlant-backend-main`)

- `entities/ContactRequestEntity` — `id`, `name`, `phone`, `message`,
  `locale`, `created_at`. Table `contact_requests`, created via Hibernate
  `ddl-auto=update` (consistent with every other table in this schema — no
  Flyway/Liquibase in this project).
- `repositories/ContactRequestRepository` — standard Spring Data JPA repo.
- `dto/contact/ContactRequestDTO` — request body: `name` (`@NotBlank`,
  `@Size(max=255)`), `phone` (`@NotBlank`, basic pattern check), `message`
  (`@NotBlank`, `@Size(max=2000)`), `locale` (optional, defaults from
  `Accept-Language` like the rest of the API).
- `service/ContactService` — orchestrates: validate → rate-limit check →
  save → best-effort Telegram send → return result.
- `service/telegram/TelegramService` — thin wrapper around
  `https://api.telegram.org/bot{token}/sendMessage`, using Spring's
  `RestClient`/`RestTemplate` (already a transitive dependency via
  `spring-boot-starter-web`; no new library needed).
- `controller/ContactController`:
  - `POST /api/v1/contact/public` — public, submits a new request.
  - `GET /api/v1/contact/admin` — ADMIN-only, lists saved requests
    (newest first). No dashboard UI is built against this in this phase —
    it exists purely so the data isn't locked away, for a future dashboard
    page.
- `util/RateLimiter` (or similar) — simple in-memory sliding-window counter
  keyed by client IP (`X-Forwarded-For` / `RemoteAddr`). No new
  infrastructure (e.g. Redis) — acceptable because the backend is a single
  always-on JVM process, not serverless.
- `config/security/SpringSecurityConfig` — add `/api/v1/contact/public` to
  the public allowlist (`openApiList`), matching how every other
  `*/public/**` path is already handled.
- New env vars (read the same way `JWT_SECRET_KEY`/AWS keys already are):
  `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`. Loaded via the existing
  `.env` + `java-dotenv` mechanism locally, or the hosting platform's env
  vars in production. `.env` is already git-ignored.

### Frontend (`atlant-frontend-main`)

- `lib/api/contact.ts` — new `submitContactRequest(data, locale)` function,
  following the exact fetch pattern already used in `lib/api/products.ts`
  / `lib/api/team.ts` (same `API_BASE_URL`, same error handling shape).
- `app/[locale]/contact/components/Contactsesion.tsx` — replace the fake
  `setTimeout` in `handleSubmit` with a real call to
  `submitContactRequest`. On failure, show an inline error (reusing the
  existing `submitted` state pattern — add a sibling `error` state) and
  **do not** clear `formData`, so the user can just hit submit again
  without retyping everything.

## Error handling

| Failure | User sees | Lead lost? |
|---|---|---|
| Telegram API down/misconfigured | Nothing — request still succeeds | No — saved in DB |
| Validation failure (e.g. empty name) | Inline error, data preserved | No — never submitted |
| Rate limit exceeded | Inline error ("too many requests, try again shortly"), data preserved | No — just throttled |
| DB save fails (e.g. DB down) | Inline error, data preserved | Yes — nothing to persist to; this is the one real failure mode surfaced to the user |
| Network failure between frontend and backend | Inline error, data preserved | No — never reached backend |

## Testing

- Backend unit tests for `ContactService` (mock `TelegramService` and
  `ContactRequestRepository`): validates save-then-notify ordering, and that
  a Telegram failure does not fail the overall request.
- Backend unit test for the rate limiter (allows N requests, rejects the
  N+1th within the window, resets after the window passes).
- Manual end-to-end check: submit the real form against a real bot
  token/chat ID and confirm the message lands in the Telegram group with
  the expected formatting.

## Explicitly out of scope (this phase)

- Admin dashboard UI for browsing saved contact requests (the `GET
  /api/v1/contact/admin` endpoint exists for this, but no frontend page is
  built against it yet).
- Any notification channel other than Telegram (email, SMS, etc.).
- Distributed/shared rate limiting (fine for a single-instance deployment;
  would need revisiting if the backend is ever horizontally scaled).
