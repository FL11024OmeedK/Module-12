# 🤖 AI_FEATURE — Employee Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Employee entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Employee Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/employee-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose full CRUD over the `employees` table through the JSON REST API. An employee links a **user**
(login identity) and an **address**, and carries contact info (`phone`, `email`). The workflow also
provides a **lookup by user id** to resolve a user's employee role. It is structurally the same as
the Customer workflow (already on `dev`), minus the `active` flag.

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the Employee vertical slice:

- **DTO** — `ApiEmployeeDTO` for request input and response output.
- **Repository** — native SQL CRUD + `findEmployeeByUserId` lookup + `getLastInsertedId`.
- **Service** — DTO-based methods (create, read all, read by id, update, delete) plus the existing
  `findEmployeeByUserId` and `mapEmployeeToDTO` helper.
- **Controller** — the five REST endpoints on `EmployeeApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure) in `EmployeeApiControllerTest`.

### Out of Scope (Excluded)

- Do **not** modify the `Employee`, `User`, or `Address` entities (frozen).
- Do **not** implement User or Address CRUD here — those are their own workflows. This feature only
  **references** their ids (FKs) and assumes they already exist.
- Do **not** change the already-working JPA methods, the `findEmployeeByUserId` service wiring, or
  the `mapEmployeeToDTO(...)` helper — reuse them.
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/filtering; no endpoints or fields beyond the API reference. **No `active` field** —
  the Employee entity/contract does not have one.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiEmployeeDTO`** — fields the mapper already sets (`setId/UserId/AddressId/Phone/Email`):
   - `int id` → `id` (response only)
   - `int userId` → `@JsonProperty("user_id")`
   - `int addressId` → `@JsonProperty("address_id")`
   - `String phone` → `phone`, `@NotBlank`
   - `String email` → `email`, `@Email` (optional)
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`EmployeeRepository` (native SQL)** — fill the `@Query` bodies (parameterized bindings;
   **positional** where no `@Param`, **named** where present):
   - `saveEmployee(userId, addressId, phone, email)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllEmployees()` → `List<Employee>`.
   - `findEmployeeById(:employeeId)` → `Optional<Employee>`.
   - `findEmployeeByUserId(:userId)` → `Optional<Employee>` (lookup).
   - `updateEmployee(employeeId, phone, email)` — `UPDATE`. Updates only `phone`, `email`
     (+ `update_on = NOW()`); does NOT touch `user_id` or `address_id`.
   - `deleteEmployeeById(:employeeId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`EmployeeService` (DTO methods)** — implement the five `// todo:` DTO methods:
   - **Create from DTO:** validate the referenced `user_id` and `address_id` exist and that the
     user does not already have an employee (one employee per user); insert; fetch new id; return
     created DTO.
   - Get all as `List<ApiEmployeeDTO>`.
   - Get by id as `Optional<ApiEmployeeDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiEmployeeDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Reuse `mapEmployeeToDTO(...)`; keep all logic in the service.

4. **`EmployeeApiController`** — declare `private final EmployeeService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). Thin controller: delegate, wrap success with
   `ResponseBuilder`, throw exceptions for errors.

5. **`EmployeeApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

Consumer is the mobile app / Postman (an authenticated API client).

1. **List:** `GET /api/employees` → all employees as DTOs → `200` array.
2. **Read one:** `GET /api/employees/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/employees` → `@Valid` passes → service checks the two FKs exist and the
   user has no employee yet → insert → fetch id → `201`. Blank `phone`/invalid `email` → `400`;
   unknown/duplicate FK → `400`.
4. **Update:** `PUT /api/employees/{id}` → id exists → update `phone`, `email` → `200`; id missing → `404`.
5. **Delete:** `DELETE /api/employees/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/employees`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/employees` | List all employees | `200` + array | — |
| GET | `/api/employees/{id}` | Get one employee | `200` | `404` |
| POST | `/api/employees` | Create employee | `201` (with `id`) | `400` invalid/missing/duplicate |
| PUT | `/api/employees/{id}` | Update employee | `200` | `404` · `400` invalid |
| DELETE | `/api/employees/{id}` | Delete employee | `200` (deleted data) | `404` |

**Files involved:**
- DTO — [`dtos/employee/ApiEmployeeDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/employee/ApiEmployeeDTO.java)
- Repository — [`repository/EmployeeRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/EmployeeRepository.java)
- Service — [`service/EmployeeService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/EmployeeService.java)
- Controller — [`controller/api/EmployeeApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/EmployeeApiController.java)
- Test — [`api/employee/EmployeeApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/employee/EmployeeApiControllerTest.java)

---

## Data Used or Modified

**Table:** `employees`. **Entity:** `Employee` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `user` (→ id) | `user_id` | `user_id` | `int` | FK → `users`, **unique** (one employee per user), NOT NULL |
| `address` (→ id) | `address_id` | `address_id` | `int` | FK → `addresses`, NOT NULL |
| `phone` | `phone` | `phone` | `String` | NOT NULL, `@NotBlank` |
| `email` | `email` | `email` | `String` | nullable, `@Email` when present |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `phone` required (not blank); `email` valid format if
  provided. A `{}` body → `400` (blank `phone`).
- **Referential validation** (service): `user_id` and `address_id` must reference existing rows; a
  user may have at most one employee. Failures → `400` (the DB unique constraint on `user_id` also
  surfaces as `400` via the `DataAccessException` handler).
- **`user_id` and `address_id` are immutable on update** — `updateEmployee` changes only `phone`
  and `email`. Those two fields in the PUT body are ignored; the response echoes their stored values.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`**, so `created_on`/`update_on` are
  set via `NOW()` on insert/update.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveEmployee(...)` then `getLastInsertedId()`
  in one `@Transactional` service method (same connection, correct `LAST_INSERT_ID()`).
- Reuse `mapEmployeeToDTO(...)` and the JPA/`findEmployeeByUserId` methods already in the service.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) and `BadRequestException` (400) — no try/catch. `@Valid` covers
  format errors.
- 404 detail message: `Employee with id {id} not found` (matches the API reference).
- DELETE returns the deleted employee DTO (the API reference's delete example shows a subset of
  fields; returning the full DTO is an acceptable superset).

---

## Acceptance Criteria

- [ ] `ApiEmployeeDTO` has `id, userId, addressId, phone, email` with correct `@JsonProperty`
      snake_case mapping, `@NotBlank` on `phone`, `@Email` on `email`, and Lombok accessors
      compatible with `mapEmployeeToDTO(...)`.
- [ ] All `EmployeeRepository` native queries implemented with parameterized bindings and correct
      `employees` columns; update leaves `user_id`/`address_id` untouched; write queries carry
      `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `EmployeeService` methods implemented; create validates FK existence +
      one-employee-per-user and returns the DTO with a generated `id`; get-by-id/update return
      `Optional.empty()` when absent; delete returns a boolean.
- [ ] `EmployeeApiController` implements all five endpoints with constructor injection, correct HTTP
      verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `EmployeeApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/employees` → 200, array.
  - [ ] `GET /api/employees/{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/employees` valid → 201 with matching fields; `{}` → 400.
  - [ ] `PUT /api/employees/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/employees/{id}` known → 200; unknown → 404.
- [ ] Tests build valid FKs (create a fresh user for `user_id`; use a seeded `address_id`), use
      `@Transactional` rollback, and avoid hard-coded ids that don't match seeded/created data.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Employee depends on User and Address rows existing.** Tests must create/seed a valid `user_id`
  (one per employee) and reference a valid `address_id`. The User API is not built yet, so create a
  fresh `User` via `UserRepository` in the test (same approach used by the Courier/Customer tests).
- Watch the asymmetry: **update ignores `user_id`/`address_id`** (only `phone`/`email` change).
  There is **no `active`** field on Employee. Match the provided repository method signatures exactly.
- Mirror the completed **Customer** workflow on `dev` — it is the closest reference pattern (Employee
  is Customer without `active`).
- Keep the diff minimal and additive: only fill the `// todo:` sections; do not refactor working code.
