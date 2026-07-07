# 🤖 AI_FEATURE — Courier Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Courier entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Courier Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/courier-table-workflow` (created from `dev`, merged back to `dev` via PR)

---

## Feature Goal

Expose full CRUD over the `couriers` table through the JSON REST API so the mobile app can manage
delivery couriers. A courier links a **user** (login identity), an **address**, and a **courier
status**, and carries contact info (`phone`, `email`) and an `active` flag. The workflow also
provides a **lookup by user id** used to resolve a user's courier role (as seen in the auth flow's
`courier_id`).

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the Courier vertical slice:

- **DTO** — `ApiCourierDTO` used for request input and response output.
- **Repository** — native SQL CRUD + `findCourierByUserId` lookup + `getLastInsertedId`.
- **Service** — DTO-based methods (create, read all, read by id, update, delete) plus the existing
  `findCourierByUserId` and `mapCourierToDTO` helper.
- **Controller** — the five REST endpoints on `CourierApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure) in `CourierApiControllerTest`.

### Out of Scope (Excluded)

- Do **not** modify the `Courier`, `User`, `Address`, or `CourierStatus` entities (frozen).
- Do **not** implement User, Address, or CourierStatus CRUD here — those are their own workflows.
  This feature only **references** their ids (FKs) and assumes they already exist.
- Do **not** change the already-working JPA methods, the `findCourierByUserId` service wiring, or
  the `mapCourierToDTO(...)` helper — reuse them.
- No auth/role logic beyond the global JWT rule (all `/api/**` except `/api/auth` require a token;
  tests bypass it with `addFilters = false`).
- No pagination/filtering/sorting; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiCourierDTO`** — fields the mapper already sets (`setId/UserId/AddressId/CourierStatusId/
   Phone/Email/Active`), with Lombok + JSON mapping + validation:
   - `int id` → `id` (response only)
   - `int userId` → `@JsonProperty("user_id")`
   - `int addressId` → `@JsonProperty("address_id")`
   - `int courierStatusId` → `@JsonProperty("courier_status_id")`
   - `String phone` → `phone`, `@NotBlank`
   - `String email` → `email`, `@Email` (optional)
   - `Boolean active` → `active`
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`CourierRepository` (native SQL)** — fill the `@Query` bodies (parameterized bindings; use
   **positional** where the method has no `@Param`, **named** where it does):
   - `saveCourier(userId, addressId, courierStatusId, phone, email)` — `INSERT`. **No `active`
     param → set `active = true` in the SQL**; also set `created_on`/`update_on` to `NOW()`.
   - `findAllCouriers()` → `List<Courier>`.
   - `findCourierById(:courierId)` → `Optional<Courier>`.
   - `findCourierByUserId(:userId)` → `Optional<Courier>` (lookup).
   - `updateCourier(courierId, courierStatusId, phone, email, active)` — `UPDATE`. **Updates only
     `courier_status_id`, `phone`, `email`, `active` (+ `update_on = NOW()`); does NOT touch
     `user_id` or `address_id`.**
   - `deleteCourierById(:courierId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`CourierService` (DTO methods)** — implement the five `// todo:` DTO methods:
   - **Create from DTO:** validate the referenced `user_id`, `address_id`, `courier_status_id`
     exist and that the user does not already have a courier (one courier per user); insert;
     fetch new id; return created DTO (with `active = true`).
   - Get all as `List<ApiCourierDTO>`.
   - Get by id as `Optional<ApiCourierDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiCourierDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Reuse `mapCourierToDTO(...)`; keep all logic in the service.

4. **`CourierApiController`** — declare `private final CourierService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). Thin controller: delegate, wrap success with
   `ResponseBuilder`, throw exceptions for errors.

5. **`CourierApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

Consumer is the mobile app / Postman (an authenticated API client).

1. **List:** `GET /api/couriers` → all couriers as DTOs → `200` array.
2. **Read one:** `GET /api/couriers/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/couriers` with a body → `@Valid` passes → service checks the three FKs
   exist and the user has no courier yet → insert (`active = true`) → fetch id → `201`. Blank
   `phone`/invalid `email` → `400`; unknown/duplicate FK → `400`.
4. **Update:** `PUT /api/couriers/{id}` → id exists → update `courier_status_id`, `phone`,
   `email`, `active` → `200`; id missing → `404`; invalid body → `400`.
5. **Delete:** `DELETE /api/couriers/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/couriers`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/couriers` | List all couriers | `200` + array | — |
| GET | `/api/couriers/{id}` | Get one courier | `200` | `404` |
| POST | `/api/couriers` | Create courier | `201` (with `id`) | `400` invalid/missing/duplicate |
| PUT | `/api/couriers/{id}` | Update courier | `200` | `404` · `400` invalid |
| DELETE | `/api/couriers/{id}` | Delete courier | `200` (deleted data) | `404` |

**Files involved:**
- DTO — [`dtos/courier/ApiCourierDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/courier/ApiCourierDTO.java)
- Repository — [`repository/CourierRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/CourierRepository.java)
- Service — [`service/CourierService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/CourierService.java)
- Controller — [`controller/api/CourierApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/CourierApiController.java)
- Test — [`api/courier/CourierApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/courier/CourierApiControllerTest.java)

---

## Data Used or Modified

**Table:** `couriers`. **Entity:** `Courier` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `user` (→ id) | `user_id` | `user_id` | `int` | FK → `users`, **unique** (one courier per user), NOT NULL |
| `address` (→ id) | `address_id` | `address_id` | `int` | FK → `addresses`, NOT NULL |
| `courierStatus` (→ id) | `courier_status_id` | `courier_status_id` | `int` | FK → `courier_statuses`, NOT NULL |
| `phone` | `phone` | `phone` | `String` | NOT NULL, `@NotBlank` |
| `email` | `email` | `email` | `String` | nullable, `@Email` when present |
| `active` | `active` | `active` | `Boolean` | NOT NULL, defaults `true` |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `phone` required (not blank); `email` valid format if
  provided. A `{}` body → `400` (blank `phone`).
- **Referential validation** (service): `user_id`, `address_id`, `courier_status_id` must
  reference existing rows; a user may have at most one courier. Failures → `400` (the DB unique
  constraint on `user_id` also surfaces as `400` via the `DataAccessException` handler).
- **`active` on create is always `true`** — the `saveCourier` native query has no `active`
  parameter and sets `active = true`. `active` is only mutable through **update**.
- **`user_id` and `address_id` are immutable on update** — `updateCourier` changes only
  `courier_status_id`, `phone`, `email`, `active`. Those two fields in the PUT body are ignored;
  the response echoes their stored values.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`**, so `created_on`/`update_on` are
  set via `NOW()` in the insert/update.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveCourier(...)` then `getLastInsertedId()`
  in one `@Transactional` service method (same connection, correct `LAST_INSERT_ID()`).
- Reuse `mapCourierToDTO(...)` and the JPA/`findCourierByUserId` methods already in the service.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) and `BadRequestException` (400) — no try/catch. `@Valid` covers
  format errors.
- 404 detail message: `Courier with id {id} not found` (matches the API reference).
- DELETE returns the deleted courier DTO (the API reference's delete example shows a subset of
  fields; returning the full DTO is an acceptable superset).

---

## Acceptance Criteria

- [ ] `ApiCourierDTO` has `id, userId, addressId, courierStatusId, phone, email, active` with
      correct `@JsonProperty` snake_case mapping, `@NotBlank` on `phone`, `@Email` on `email`, and
      Lombok accessors compatible with `mapCourierToDTO(...)`.
- [ ] All `CourierRepository` native queries implemented with parameterized bindings and correct
      `couriers` columns; create sets `active = true`; update leaves `user_id`/`address_id`
      untouched; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `CourierService` methods implemented; create validates FK existence +
      one-courier-per-user and returns the DTO with a generated `id`; get-by-id/update return
      `Optional.empty()` when absent; delete returns a boolean.
- [ ] `CourierApiController` implements all five endpoints with constructor injection, correct HTTP
      verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException` on
      missing ids.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `CourierApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/couriers` → 200, array.
  - [ ] `GET /api/couriers/{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/couriers` valid → 201 with matching fields; `{}` → 400.
  - [ ] `PUT /api/couriers/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/couriers/{id}` known → 200; unknown → 404.
- [ ] Tests build valid FKs (create a fresh user for `user_id`; use seeded `address_id` and
      `courier_status_id`), use `@Transactional` rollback, and avoid hard-coded ids that don't
      match seeded/created data.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Courier depends on User, Address, and CourierStatus rows existing.** Tests must create/seed a
  valid `user_id` (one per courier), and reference a valid `address_id` and `courier_status_id`.
- Watch the two asymmetries: **create ignores `active` (always true)** and **update ignores
  `user_id`/`address_id`**. Match the provided repository method signatures exactly — do not add
  parameters to them.
- Study the implemented `OrderApiController`/`OrderStatusApiController` and the completed **Address**
  workflow (already on `dev`) as the reference pattern.
- Keep the diff minimal and additive: only fill the `// todo:` sections; do not refactor working code.
