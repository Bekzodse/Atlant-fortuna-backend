# Contact Form → Telegram Notification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Submitting the contact form at `/{locale}/contact` saves the lead in Postgres and notifies the marketing team's Telegram group, replacing the current fake `setTimeout` that sends nothing anywhere.

**Architecture:** New `POST /api/v1/contact/public` endpoint on the existing Spring Boot backend (`atlant-backend-main`), following this codebase's established `controller → service → repository → entity` layering. The service saves to Postgres first (durable), then best-effort notifies Telegram (failure there is logged, never fails the request or loses the lead). The frontend (`atlant-frontend-main`) calls this endpoint and shows a retryable error on failure, preserving the user's entered data.

**Tech Stack:** Spring Boot 3.5.6 / Java 17 (`spring-boot-starter-web`'s `RestClient` for the Telegram HTTP call — no new dependency), Spring Data JPA, Jakarta Bean Validation, JUnit 5 + Mockito (already in `spring-boot-starter-test`). Next.js 15 / TypeScript on the frontend (no test framework configured there — frontend verification is manual).

**Spec:** `docs/superpowers/specs/2026-10-08-contact-form-telegram-design.md`

## Global Constraints

- Public endpoint: `POST /api/v1/contact/public`, no auth — must be added to `SpringSecurityConfig.openApiList`.
- Admin endpoint: `GET /api/v1/contact/admin` — must require `hasRole("ADMIN")`, same as every other `*/admin/**` path in `SpringSecurityConfig`.
- Rate limit: max **5 requests per 60 seconds per client IP**, in-memory (no new infra — single-instance backend).
- DB save happens **before** the Telegram send attempt; a Telegram failure must never fail the request or lose the lead.
- Telegram message format (exact template from spec):
  ```
  🆕 Новая заявка с сайта

  👤 Имя: {name}
  📞 Телефон: {phone}
  💬 Сообщение: {message}
  🌐 Страница: {locale}/contact
  🕒 Время: {yyyy-MM-dd HH:mm}
  ```
- New env vars `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`, read via `application.properties` placeholders exactly like `JWT_SECRET_KEY`/AWS keys already are (`${TELEGRAM_BOT_TOKEN}` etc.), loaded locally via the existing `.env` + `java-dotenv` mechanism.
- Frontend: on a failed submission, preserve the entered `formData` (do not reset it) and show a retryable inline error.
- Admin dashboard UI for browsing submissions is explicitly **out of scope** this phase — only the `GET /api/v1/contact/admin` data endpoint is built.

## Review Focus

- Blank/missing `name`, `phone`, or `message` in the POST body → rejected with a 400 and field errors, not silently accepted. (Task 1 test)
- Telegram API down, misconfigured token, or any exception from the Telegram call → the request still succeeds and the lead is still saved; only the DB save is allowed to fail the request. (Task 4 test)
- More than 5 requests/minute from the same IP → rejected before touching the database or Telegram, not silently rate-limited with no feedback. (Task 2 + Task 5 tests)
- Database save failure (e.g. DB unreachable) → the one failure mode that *should* propagate as an error to the user, and must not also attempt a Telegram send for a request that was never durably saved. (Task 4 test)
- User resubmitting after a failed attempt → their typed name/phone/message must still be in the form fields, not wiped by the failed attempt. (Task 7, manual verification step)

---

### Task 1: Contact data layer (entity, repository, DTOs)

**Files:**
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/entities/ContactRequestEntity.java`
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/repositories/ContactRequestRepository.java`
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/dto/contact/ContactRequestDTO.java`
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/dto/contact/ContactResponseDTO.java`
- Test: `atlant-backend-main/src/test/java/sayyeed/dev/atlantbackend/dto/contact/ContactRequestDTOTest.java`

**Interfaces:**
- Produces:
  - `ContactRequestEntity` — fields `id` (`String`, `@Id @GeneratedValue(strategy = GenerationType.UUID)`), `name` (`String`), `phone` (`String`), `message` (`String`), `locale` (`String`), `createdAt` (`LocalDateTime`, `@CreationTimestamp`, column `created_at`, `nullable = false, updatable = false`). `@Entity @Table(name = "contact_requests")`, Lombok `@Getter @Setter`. Follow the exact shape of `GalleryEntity` (`atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/entities/GalleryEntity.java`) for the id/createdAt pattern.
  - `ContactRequestRepository extends CrudRepository<ContactRequestEntity, String>` with method `List<ContactRequestEntity> findAllByOrderByCreatedAtDesc()`.
  - `ContactRequestDTO` — Lombok `@Getter @Setter` (not `@Getter`-only — this DTO needs setters so Jackson can actually populate it from the incoming JSON body, and so it's constructible in tests). Fields: `name` (`@NotBlank(message = "Name is required")`, `@Size(max = 255, message = "Name is too long")`), `phone` (`@NotBlank(message = "Phone is required")`, `@Pattern(regexp = "^[+]?[0-9\\s()-]{6,20}$", message = "Phone number format is invalid")`), `message` (`@NotBlank(message = "Message is required")`, `@Size(max = 2000, message = "Message is too long")`).
  - `ContactResponseDTO` — Lombok `@Getter @Setter`, fields: `id`, `name`, `phone`, `message`, `locale` (all `String`), `createdAt` (`LocalDateTime`). Mirror `GalleryResponseDTO` (`atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/dto/gallery/GalleryResponseDTO.java`).

- [ ] **Step 1: Write the failing validation test**

```java
package sayyeed.dev.atlantbackend.dto.contact;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactRequestDTOTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankName() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("");
        dto.setPhone("+998901234567");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void rejectsInvalidPhone() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("not-a-phone-number!!");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void acceptsValidPayload() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Hello, I have a question");

        Set<ConstraintViolation<ContactRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
```

- [ ] **Step 2: Run test to verify it fails (class doesn't exist yet)**

Run: `./mvnw test -Dtest=ContactRequestDTOTest -pl . ` (from `atlant-backend-main/`)
Expected: FAIL — compilation error, `ContactRequestDTO` does not exist.

- [ ] **Step 3: Create `ContactRequestEntity`, `ContactRequestRepository`, `ContactRequestDTO`, `ContactResponseDTO`**

Exact shapes as specified in the Interfaces block above.

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=ContactRequestDTOTest`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
cd atlant-backend-main
git add src/main/java/sayyeed/dev/atlantbackend/entities/ContactRequestEntity.java \
        src/main/java/sayyeed/dev/atlantbackend/repositories/ContactRequestRepository.java \
        src/main/java/sayyeed/dev/atlantbackend/dto/contact/ContactRequestDTO.java \
        src/main/java/sayyeed/dev/atlantbackend/dto/contact/ContactResponseDTO.java \
        src/test/java/sayyeed/dev/atlantbackend/dto/contact/ContactRequestDTOTest.java
git commit -m "feat: add contact request entity, repository, and DTOs"
```

---

### Task 2: Rate limiter utility

**Files:**
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/utils/RateLimiter.java`
- Test: `atlant-backend-main/src/test/java/sayyeed/dev/atlantbackend/utils/RateLimiterTest.java`

**Interfaces:**
- Produces: `@Component class RateLimiter` with:
  - `public RateLimiter()` — default constructor, delegates to the 2-arg constructor with `maxRequests = 5`, `windowMillis = 60_000L` (the Global Constraint value). This is the constructor Spring uses (Spring auto-selects the no-arg constructor when multiple constructors exist and none is annotated `@Autowired`).
  - `RateLimiter(int maxRequests, long windowMillis)` — package-private, used only by the test to inject a short window.
  - `public synchronized boolean allow(String key)` — returns `true` and records the request if `key` has made fewer than `maxRequests` requests within the trailing `windowMillis`; otherwise returns `false` without recording. Independent counters per `key`. Backed by `ConcurrentHashMap<String, Deque<Long>>` of request timestamps (millis), pruning entries older than `windowMillis` on each call.

- [ ] **Step 1: Write the failing tests**

```java
package sayyeed.dev.atlantbackend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterTest {

    @Test
    void allowsUpToMaxRequestsThenRejects() {
        RateLimiter limiter = new RateLimiter(2, 100_000L);

        assertTrue(limiter.allow("1.2.3.4"));
        assertTrue(limiter.allow("1.2.3.4"));
        assertFalse(limiter.allow("1.2.3.4"));
    }

    @Test
    void resetsAfterWindowPasses() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(1, 100L);

        assertTrue(limiter.allow("5.6.7.8"));
        assertFalse(limiter.allow("5.6.7.8"));

        Thread.sleep(150);

        assertTrue(limiter.allow("5.6.7.8"));
    }

    @Test
    void tracksDifferentKeysIndependently() {
        RateLimiter limiter = new RateLimiter(1, 100_000L);

        assertTrue(limiter.allow("1.1.1.1"));
        assertTrue(limiter.allow("2.2.2.2"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest=RateLimiterTest`
Expected: FAIL — compilation error, `RateLimiter` does not exist.

- [ ] **Step 3: Implement `RateLimiter` in `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/utils/RateLimiter.java`**

As specified in the Interfaces block. Use `Deque<Long>` (e.g. `ArrayDeque`) per key; on each `allow()` call, poll from the front while the oldest timestamp is older than `windowMillis`, then check size against `maxRequests` before adding the new timestamp. The whole check-and-record must happen under the method's `synchronized` lock to avoid a race between two concurrent requests from the same key both passing the check.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest=RateLimiterTest`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
cd atlant-backend-main
git add src/main/java/sayyeed/dev/atlantbackend/utils/RateLimiter.java \
        src/test/java/sayyeed/dev/atlantbackend/utils/RateLimiterTest.java
git commit -m "feat: add in-memory per-key rate limiter"
```

---

### Task 3: Telegram integration

**Files:**
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/service/telegram/ITelegramService.java`
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/service/telegram/impl/TelegramServiceImpl.java`
- Modify: `atlant-backend-main/src/main/resources/application.properties`

**Interfaces:**
- Produces:
  - `ITelegramService` — interface with `void sendMessage(String text)`. Throws on failure (callers decide whether to swallow it); does not know anything about "contact requests" — it's a generic text-sender.
  - `TelegramServiceImpl implements ITelegramService` — `@Service`, reads `@Value("${telegram.bot.token}") String botToken` and `@Value("${telegram.chat.id}") String chatId`. Calls `POST https://api.telegram.org/bot{botToken}/sendMessage` with a JSON body `{"chat_id": chatId, "text": text}` using a `RestClient` instance (`RestClient.create()`), via `.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(Map.of("chat_id", chatId, "text", text)).retrieve().toBodilessEntity()`. Let `RestClientResponseException` propagate on non-2xx (do not catch here — Task 4's caller decides how to handle failure).

No dedicated unit test for this class: this codebase has no existing tests for its other thin external-API wrappers either (e.g. `S3ServiceImpl`), and its actual behavior (does a Telegram failure fail the request?) is exercised through the `ITelegramService` mock in Task 4's `ContactServiceTest`, which is the seam that matters.

- [ ] **Step 1: Create `ITelegramService` and `TelegramServiceImpl`**

As specified in the Interfaces block.

- [ ] **Step 2: Add Telegram config to `application.properties`**

Add after the existing `#-----------JWT--------------------------------` block:
```properties
#-----------Telegram----------------------------
telegram.bot.token=${TELEGRAM_BOT_TOKEN}
telegram.chat.id=${TELEGRAM_CHAT_ID}
```

- [ ] **Step 3: Verify the project still compiles**

Run: `./mvnw compile`
Expected: BUILD SUCCESS. (No automated test for this task — see note above.)

- [ ] **Step 4: Commit**

```bash
cd atlant-backend-main
git add src/main/java/sayyeed/dev/atlantbackend/service/telegram/ITelegramService.java \
        src/main/java/sayyeed/dev/atlantbackend/service/telegram/impl/TelegramServiceImpl.java \
        src/main/resources/application.properties
git commit -m "feat: add Telegram bot sendMessage integration"
```

---

### Task 4: Contact service (orchestration)

**Files:**
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/service/ContactService.java`
- Test: `atlant-backend-main/src/test/java/sayyeed/dev/atlantbackend/service/ContactServiceTest.java`

**Interfaces:**
- Consumes: `ContactRequestRepository` (Task 1: `save(ContactRequestEntity)`, `findAllByOrderByCreatedAtDesc()`), `ContactRequestEntity`/`ContactRequestDTO`/`ContactResponseDTO` (Task 1), `ITelegramService.sendMessage(String)` (Task 3).
- Produces: `@Service class ContactService` with:
  - `public ContactResponseDTO submit(ContactRequestDTO dto, String locale)` — builds a `ContactRequestEntity` from `dto` + `locale`, saves it via the repository (let any exception propagate — do not catch), then calls `telegramService.sendMessage(...)` wrapped in its own `try/catch (Exception e)` that only logs (`org.slf4j.Logger`, e.g. `LoggerFactory.getLogger(ContactService.class)`) — never rethrows. Returns the saved entity mapped to `ContactResponseDTO`.
  - `public List<ContactResponseDTO> getAll()` — returns all saved requests via `findAllByOrderByCreatedAtDesc()`, mapped to `ContactResponseDTO`.
  - Private `formatTelegramMessage(ContactRequestDTO dto, String locale)` — builds the exact template from Global Constraints, using `LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))` for the timestamp (deliberately *not* `entity.getCreatedAt()`, which is only populated by Hibernate against a real persistence context and would be `null` in unit tests / before a real flush).

- [ ] **Step 1: Write the failing tests**

```java
package sayyeed.dev.atlantbackend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.dto.contact.ContactResponseDTO;
import sayyeed.dev.atlantbackend.entities.ContactRequestEntity;
import sayyeed.dev.atlantbackend.repositories.ContactRequestRepository;
import sayyeed.dev.atlantbackend.service.telegram.ITelegramService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {
    @Mock private ContactRequestRepository contactRequestRepository;
    @Mock private ITelegramService telegramService;
    @InjectMocks private ContactService contactService;

    private ContactRequestDTO validDto() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Interested in RO-500");
        return dto;
    }

    @Test
    void submit_savesEntityAndSendsTelegramMessage() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactResponseDTO response = contactService.submit(validDto(), "uz");

        verify(contactRequestRepository).save(any(ContactRequestEntity.class));
        verify(telegramService).sendMessage(contains("Aziz"));
        assertEquals("Aziz", response.getName());
    }

    @Test
    void submit_whenTelegramFails_stillSucceedsAndEntityIsSaved() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Telegram down")).when(telegramService).sendMessage(any());

        assertDoesNotThrow(() -> contactService.submit(validDto(), "uz"));
        verify(contactRequestRepository).save(any(ContactRequestEntity.class));
    }

    @Test
    void submit_whenRepositorySaveFails_propagatesAndSkipsTelegram() {
        when(contactRequestRepository.save(any(ContactRequestEntity.class)))
                .thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> contactService.submit(validDto(), "uz"));
        verify(telegramService, never()).sendMessage(any());
    }

    @Test
    void getAll_returnsSavedRequestsOrderedNewestFirst() {
        ContactRequestEntity entity = new ContactRequestEntity();
        entity.setName("Aziz");
        when(contactRequestRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(entity));

        List<ContactResponseDTO> result = contactService.getAll();

        assertEquals(1, result.size());
        assertEquals("Aziz", result.get(0).getName());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest=ContactServiceTest`
Expected: FAIL — compilation error, `ContactService` does not exist.

- [ ] **Step 3: Implement `ContactService`**

As specified in the Interfaces block.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./mvnw test -Dtest=ContactServiceTest`
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
cd atlant-backend-main
git add src/main/java/sayyeed/dev/atlantbackend/service/ContactService.java \
        src/test/java/sayyeed/dev/atlantbackend/service/ContactServiceTest.java
git commit -m "feat: add ContactService — save then best-effort Telegram notify"
```

---

### Task 5: Contact controller + security wiring

**Files:**
- Create: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/controller/ContactController.java`
- Modify: `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/config/security/SpringSecurityConfig.java`
- Test: `atlant-backend-main/src/test/java/sayyeed/dev/atlantbackend/controller/ContactControllerTest.java`

**Interfaces:**
- Consumes: `ContactService.submit(ContactRequestDTO, String)` / `.getAll()` (Task 4), `RateLimiter.allow(String)` (Task 2), `AppBadException` (existing, `sayyeed.dev.atlantbackend.exceptions.AppBadException` — already mapped to HTTP 400 by the existing `ExceptionHandlerController`).
- Produces: `@RestController @RequestMapping("/api/v1/contact") class ContactController` with:
  - `public ResponseEntity<String> submit(@Valid @RequestBody ContactRequestDTO dto, @RequestHeader(value = "Accept-Language", defaultValue = "ru") String locale, HttpServletRequest request)` mapped `@PostMapping("/public")`. Extracts the client IP via a private `extractClientIp(HttpServletRequest request)` helper (first entry of `X-Forwarded-For` header if present and non-blank, else `request.getRemoteAddr()`). If `rateLimiter.allow(clientIp)` is `false`, throws `new AppBadException("Too many requests, please try again later")` *before* calling `contactService.submit(...)`. Otherwise calls `contactService.submit(dto, locale)` and returns `ResponseEntity.ok("Request submitted successfully")`.
  - `public ResponseEntity<List<ContactResponseDTO>> getAll()` mapped `@GetMapping("/admin")`, returns `ResponseEntity.ok(contactService.getAll())`.

- [ ] **Step 1: Write the failing tests**

```java
package sayyeed.dev.atlantbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sayyeed.dev.atlantbackend.dto.contact.ContactRequestDTO;
import sayyeed.dev.atlantbackend.exceptions.AppBadException;
import sayyeed.dev.atlantbackend.service.ContactService;
import sayyeed.dev.atlantbackend.utils.RateLimiter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {
    @Mock private ContactService contactService;
    @Mock private RateLimiter rateLimiter;
    @Mock private HttpServletRequest request;
    @InjectMocks private ContactController contactController;

    private ContactRequestDTO validDto() {
        ContactRequestDTO dto = new ContactRequestDTO();
        dto.setName("Aziz");
        dto.setPhone("+998901234567");
        dto.setMessage("Hi");
        return dto;
    }

    @Test
    void submit_whenRateLimited_throwsAppBadExceptionAndSkipsService() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(rateLimiter.allow("9.9.9.9")).thenReturn(false);

        assertThrows(AppBadException.class,
                () -> contactController.submit(validDto(), "ru", request));
        verify(contactService, never()).submit(any(), any());
    }

    @Test
    void submit_whenAllowed_callsServiceAndReturnsOk() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("9.9.9.9");
        when(rateLimiter.allow("9.9.9.9")).thenReturn(true);

        ContactRequestDTO dto = validDto();
        ResponseEntity<String> response = contactController.submit(dto, "ru", request);

        verify(contactService).submit(eq(dto), eq("ru"));
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void submit_prefersXForwardedForOverRemoteAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1, 2.2.2.2");
        when(rateLimiter.allow("1.1.1.1")).thenReturn(true);

        contactController.submit(validDto(), "ru", request);

        verify(rateLimiter).allow("1.1.1.1");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -Dtest=ContactControllerTest`
Expected: FAIL — compilation error, `ContactController` does not exist.

- [ ] **Step 3: Implement `ContactController`**

As specified in the Interfaces block.

- [ ] **Step 4: Wire `SpringSecurityConfig`**

In `atlant-backend-main/src/main/java/sayyeed/dev/atlantbackend/config/security/SpringSecurityConfig.java`:
- Add `"/api/v1/contact/public"` to the `openApiList` array (alongside the existing `/api/v1/team/public/**` etc. entries).
- Add `.requestMatchers("/api/v1/contact/admin/**").hasRole("ADMIN")` to the chain of `.requestMatchers(...).hasRole("ADMIN")` calls, in the same style as the existing `/api/v1/team/admin/**` line.

- [ ] **Step 5: Run tests to verify they pass**

Run: `./mvnw test -Dtest=ContactControllerTest`
Expected: PASS (3 tests)

- [ ] **Step 6: Run the full backend test suite**

Run: `./mvnw test`
Expected: BUILD SUCCESS, all tests pass (including Tasks 1, 2, 4's tests and the pre-existing `AtlantBackendApplicationTests`).

- [ ] **Step 7: Commit**

```bash
cd atlant-backend-main
git add src/main/java/sayyeed/dev/atlantbackend/controller/ContactController.java \
        src/main/java/sayyeed/dev/atlantbackend/config/security/SpringSecurityConfig.java \
        src/test/java/sayyeed/dev/atlantbackend/controller/ContactControllerTest.java
git commit -m "feat: expose POST /api/v1/contact/public and GET /api/v1/contact/admin"
```

---

### Task 6: Frontend API client

**Files:**
- Create: `atlant-frontend-main/lib/api/contact.ts`

**Interfaces:**
- Consumes: `POST /api/v1/contact/public` (Task 5) — request body `{ name: string, phone: string, message: string }`, header `Accept-Language: {locale}`; `200` on success, non-2xx (400/429-as-400/500) on failure.
- Produces: `export interface ContactRequestPayload { name: string; phone: string; message: string; }` and `export async function submitContactRequest(payload: ContactRequestPayload, locale: string = 'ru'): Promise<void>`. Unlike the existing `fetch*` helpers in this directory (e.g. `fetchTeamMembers`), this function does **not** swallow errors into a default return value — it `throw`s when `!response.ok`, because Task 7's caller needs to distinguish success from failure to decide whether to show the error state.

- [ ] **Step 1: Create `lib/api/contact.ts`**

```ts
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://atlant-backend-production.up.railway.app';

export interface ContactRequestPayload {
  name: string;
  phone: string;
  message: string;
}

export async function submitContactRequest(
  payload: ContactRequestPayload,
  locale: string = 'ru'
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/api/v1/contact/public`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept-Language': locale,
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error(`Failed to submit contact request: ${response.status}`);
  }
}
```

- [ ] **Step 2: Typecheck**

Run (from `atlant-frontend-main/`): `npx tsc --noEmit -p tsconfig.json`
Expected: no errors.

- [ ] **Step 3: Commit**

```bash
cd atlant-frontend-main
git add lib/api/contact.ts
git commit -m "feat: add submitContactRequest API client"
```

---

### Task 7: Wire the contact form to the real endpoint

**Files:**
- Modify: `atlant-frontend-main/app/[locale]/contact/components/Contactsesion.tsx`
- Modify: `atlant-frontend-main/messages/en.json`
- Modify: `atlant-frontend-main/messages/ru.json`
- Modify: `atlant-frontend-main/messages/uz.json`

**Interfaces:**
- Consumes: `submitContactRequest(payload, locale)` (Task 6).

- [ ] **Step 1: Add an `error` translation block to all three locale files**

Add alongside the existing `contact.form.success` block (same nesting level), e.g. in `ru.json`:
```json
"error": {
  "title": "Что-то пошло не так",
  "message": "Не удалось отправить заявку. Попробуйте ещё раз или позвоните нам напрямую."
}
```
Use natural EN/UZ translations of the same meaning in `en.json`/`uz.json` (matching the tone of the existing `success` block in each file).

- [ ] **Step 2: Wire `Contactsesion.tsx`**

In `atlant-frontend-main/app/[locale]/contact/components/Contactsesion.tsx`:
- Import `submitContactRequest` from `@/lib/api/contact`.
- Remove the `email` reference risk: `formData` already has no `email` field (removed in a prior change) — confirm it's still just `{ name, phone, message }`.
- Add `const [error, setError] = useState(false);` alongside the existing `isSubmitting`/`submitted` state.
- Replace the body of `handleSubmit`:
```ts
const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(false);

    try {
        await submitContactRequest(formData, locale);
        setSubmitted(true);
        setFormData({ name: "", phone: "", message: "" });
        setTimeout(() => setSubmitted(false), 5000);
    } catch {
        setError(true);
    } finally {
        setIsSubmitting(false);
    }
};
```
- Inside the form's JSX (the `!submitted` branch), right after the `<h2>{t("form.title")}</h2>` line, add a conditional error banner:
```tsx
{error && (
    <div className="mb-6 rounded-2xl border-2 border-red-300 bg-red-50 p-4 text-red-700">
        <p className="font-semibold">{t("form.error.title")}</p>
        <p className="text-sm">{t("form.error.message")}</p>
    </div>
)}
```

- [ ] **Step 3: Typecheck**

Run (from `atlant-frontend-main/`): `npx tsc --noEmit -p tsconfig.json`
Expected: no errors.

- [ ] **Step 4: Manual verification — happy path**

With the backend running locally (`TELEGRAM_BOT_TOKEN`/`TELEGRAM_CHAT_ID` set in its `.env`) and the frontend dev server pointed at it (`NEXT_PUBLIC_API_URL=http://localhost:8080`), open `/uz/contact`, fill in name/phone/message, submit, and confirm: (a) the success screen appears, (b) a new row exists via `GET /api/v1/contact/admin` (as an authenticated admin, or by checking the DB directly), (c) the formatted message arrived in the real Telegram group.

- [ ] **Step 5: Manual verification — failure path preserves data (Review Focus item)**

Stop the local backend (or set `NEXT_PUBLIC_API_URL` to an unreachable URL), fill in the form with distinct test values, submit, and confirm: (a) the error banner appears, (b) the name/phone/message fields still contain exactly what was typed — not cleared.

- [ ] **Step 6: Commit**

```bash
cd atlant-frontend-main
git add app/\[locale\]/contact/components/Contactsesion.tsx messages/en.json messages/ru.json messages/uz.json
git commit -m "feat: submit contact form to backend, show retryable error on failure"
```
