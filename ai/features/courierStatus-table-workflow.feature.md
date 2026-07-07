# 🤖 AI_FEATURE — CourierStatus Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the CourierStatus entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** CourierStatus Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/courierStatus-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose full CRUD over the `courier_statuses` table through the JSON REST API. Courier statuses are
a small **reference/lookup table** (seeded values: `free`, `busy`, `full`, `offline`) that couriers
point to via `courier_status_id`. This is the simplest vertical slice — a single `name` field — and
follows the same pattern as the Address and OrderStatus workflows.

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the CourierStatus vertical slice:

- **DTO** — `ApiCourierStatusDTO` (`id`, `name`) for request input and response output.
- **Repository** — native SQL CRUD + `getLastInsertedId`.
- **Service** — DTO-based methods (create, read all, read by id, update, delete) reusing the
  existing `mapCourierStatusToDTO` helper.
- **Controller** — the five REST endpoints on `CourierStatusApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `CourierStatus` entity (frozen).
- Do **not** touch the Courier workflow (already on `dev`) — this feature only provides the status
  rows that couriers reference; it does not manage couriers.
- No enforcement of a fixed status vocabulary (any non-blank `name` is allowed); no uniqueness rule
  on `name` (the entity defines none).
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/filtering; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiCourierStatusDTO`** — fields the mapper already sets (`setId`, `setName`):
   - `int id` → `id` (response only)
   - `String name` → `name`, `@NotBlank`
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`. (No `@JsonProperty` needed —
     both fields are single words identical in Java and JSON.)

2. **`CourierStatusRepository` (native SQL)** — fill the `@Query` bodies (parameterized bindings;
   **positional** where no `@Param`, **named** where present):
   - `saveCourierStatus(name)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllCourierStatuses()` → `List<CourierStatus>`.
   - `findCourierStatusById(:courierStatusId)` → `Optional<CourierStatus>`.
   - `updateCourierStatus(courierStatusId, name)` — `UPDATE` (+ `update_on = NOW()`).
   - `deleteCourierStatusById(:courierStatusId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`CourierStatusService` (DTO methods)** — implement the five `// todo:` DTO methods:
   - Create from DTO → insert → fetch new id → return created DTO (with `id`).
   - Get all as `List<ApiCourierStatusDTO>`.
   - Get by id as `Optional<ApiCourierStatusDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiCourierStatusDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Reuse `mapCourierStatusToDTO(...)`; keep all logic in the service.

4. **`CourierStatusApiController`** — declare `private final CourierStatusService`,
   constructor-inject it, and implement the five endpoints (see Interfaces). Thin controller:
   delegate, wrap success with `ResponseBuilder`, throw exceptions for errors.

5. **`CourierStatusApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

Consumer is the mobile app / Postman (an authenticated API client).

1. **List:** `GET /api/courier-statuses` → all statuses as DTOs → `200` array.
2. **Read one:** `GET /api/courier-statuses/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/courier-statuses` with `{ "name": "..." }` → `@Valid` passes → insert →
   fetch id → `201`. Blank/missing `name` → `400`.
4. **Update:** `PUT /api/courier-statuses/{id}` → id exists → update `name` → `200`; id missing → `404`.
5. **Delete:** `DELETE /api/courier-statuses/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/courier-statuses` (kebab-case). Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/courier-statuses` | List all statuses | `200` + array | — |
| GET | `/api/courier-statuses/{id}` | Get one status | `200` | `404` |
| POST | `/api/courier-statuses` | Create status | `201` (with `id`) | `400` blank/missing name |
| PUT | `/api/courier-statuses/{id}` | Update status | `200` | `404` |
| DELETE | `/api/courier-statuses/{id}` | Delete status | `200` (deleted data) | `404` |

**Files involved:**
- DTO — [`dtos/courierStatus/ApiCourierStatusDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/courierStatus/ApiCourierStatusDTO.java)
- Repository — [`repository/CourierStatusRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/CourierStatusRepository.java)
- Service — [`service/CourierStatusService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/CourierStatusService.java)
- Controller — [`controller/api/CourierStatusApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/CourierStatusApiController.java)
- Test — [`api/courierStatus/CourierStatusApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/courierStatus/CourierStatusApiControllerTest.java)

---

## Data Used or Modified

**Table:** `courier_statuses`. **Entity:** `CourierStatus` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `name` | `name` | `name` | `String` | NOT NULL, `@NotBlank` |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- `name` required (not blank) via `@Valid` → `{}` body → `400`.
- Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`, so `created_on`/`update_on` are set
  via `NOW()` on insert/update.
- **FK note:** `courier_statuses` is referenced by `couriers.courier_status_id`. Deleting a status
  that is still referenced by a courier violates the FK constraint and surfaces as `400` (via the
  `DataAccessException` handler). The delete test uses a **freshly created** status, so it is
  unreferenced and deletes cleanly.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveCourierStatus(name)` then
  `getLastInsertedId()` in one `@Transactional` service method (same connection, correct id).
- Reuse `mapCourierStatusToDTO(...)` and the existing JPA methods.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) — no try/catch. `@Valid` covers the blank-name case.
- 404 detail message: `Courier status with id {id} not found` (matches the API reference).

---

## Acceptance Criteria

- [ ] `ApiCourierStatusDTO` has `id` and `name`, `@NotBlank` on `name`, Lombok accessors compatible
      with `mapCourierStatusToDTO(...)`.
- [ ] All `CourierStatusRepository` native queries implemented with parameterized bindings and the
      correct `courier_statuses` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `CourierStatusService` methods implemented; create returns the DTO with a
      generated `id`; get-by-id/update return `Optional.empty()` when absent; delete returns a boolean.
- [ ] `CourierStatusApiController` implements all five endpoints with constructor injection, correct
      verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `CourierStatusApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/courier-statuses` → 200, array.
  - [ ] `GET /api/courier-statuses/{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/courier-statuses` valid → 201 with matching `name`; `{}` → 400.
  - [ ] `PUT /api/courier-statuses/{id}` valid+known → 200 reflecting the new name; unknown → 404.
  - [ ] `DELETE /api/courier-statuses/{id}` known → 200; unknown → 404.
- [ ] Tests use `@Transactional` rollback and seeded/created ids (id `1` exists from seeding);
      delete test creates a fresh status first.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- This is the reference "simple lookup table" slice — mirror the **Address** and **OrderStatus**
  patterns already on `dev`.
- Keep the diff minimal and additive: only fill the `// todo:` sections; do not refactor working code.
- Endpoint base path is **kebab-case** `/api/courier-statuses`.
