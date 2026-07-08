# 🤖 AI_FEATURE — User Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the User entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** User Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/user-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose CRUD over the `users` table through the JSON REST API. A user is the login identity (name,
unique email, password) that customers, couriers, employees, and restaurants reference. Password is
accepted on create/update but **never returned**. This is the final workflow.

This workflow is **partially provided**. Already implemented (do NOT modify): the `ApiUserDTO`
(`{id, name, email}`) and `ApiCreateUserDTO` (`{name, email, password}`) DTOs, the account DTOs, the
`findUserByEmail` native query, the custom account endpoints (`GET /api/account/{id}`,
`PUT /api/account/{id}`), the `getAccountDTO`/`updateAccount` service methods, and `mapUserToDTO`.

---

## Feature Scope

### In Scope (Included)

Complete the remaining `// todo:` stubs (the CRUD side):

- **DTOs** — already complete (`ApiUserDTO`, `ApiCreateUserDTO`); no changes needed.
- **Repository** — native SQL `saveUser`, `findAllUsers`, `findUserById`, `updateUser`,
  `deleteUserById`, `getLastInsertedId`. (`findUserByEmail` is done.)
- **Service** — DTO-based create/read-all/read-by-id/update/delete, reusing `mapUserToDTO`.
- **Controller** — the five CRUD endpoints on `UserApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `User` entity (frozen; implements `UserDetails`).
- Do **not** modify the provided account DTOs/endpoints/service methods or `findUserByEmail`.
- Do **not** implement Customer/Courier/Employee/Restaurant CRUD — users are only referenced by them.
- No auth/login logic (that is the provided `AuthApiController`); tests bypass JWT with `addFilters = false`.
- No pagination/filtering; no endpoints or fields beyond the API reference. **Password is never
  returned** in any response.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **DTOs** — verify (already complete):
   - `ApiUserDTO` (response): `id`, `name`, `email`.
   - `ApiCreateUserDTO` (create/update request): `name`, `email` (`@Email`), `password` — all required.

2. **`UserRepository` (native SQL)** — fill the `@Query` bodies (positional where no `@Param`, named
   where present):
   - `saveUser(name, email, password)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllUsers()` → `List<User>`.
   - `findUserById(:userId)` → `Optional<User>`.
   - `updateUser(userId, name, email, password)` — `UPDATE` (+ `update_on = NOW()`).
   - `deleteUserById(:userId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`UserService` (DTO methods)** — implement the `// todo:` methods, reusing `mapUserToDTO`:
   - **Create from DTO:** insert; fetch new id; return the created DTO (`id`, `name`, `email`;
     never password).
   - Get all as `List<ApiUserDTO>`.
   - Get by id as `Optional<ApiUserDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiUserDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Keep all logic in the service; `@Transactional` on create/update/delete.

4. **`UserApiController`** — implement the five CRUD endpoints under `/api/users`, delegating to the
   service, wrapping success with `ResponseBuilder`, throwing `ResourceNotFoundException` (404). Keep
   the provided custom account endpoints.

5. **`UserApiControllerTest`** — complete the stubbed CRUD tests (success + failure).

---

## User Flow / Logic (High Level)

1. **List:** `GET /api/users` → all users as DTOs → `200` array.
2. **Read one:** `GET /api/users/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/users` with `{name, email, password}` → `@Valid` passes → insert → `201`
   (returns `id`, `name`, `email`). Missing field / invalid email → `400`.
4. **Update:** `PUT /api/users/{id}` → id exists → update `name`, `email`, `password` → `200`; id
   missing → `404`.
5. **Delete:** `DELETE /api/users/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/users`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error | Status |
|--------|----------|---------|---------|-------|--------|
| GET | `/api/users` | List all users | `200` + array | — | **new** |
| GET | `/api/users/{id}` | Get one user | `200` | `404` | **new** |
| POST | `/api/users` | Create user | `201` (with `id`) | `400` invalid/missing | **new** |
| PUT | `/api/users/{id}` | Update user | `200` | `404` | **new** |
| DELETE | `/api/users/{id}` | Delete user | `200` (deleted data) | `404` | **new** |
| GET | `/api/account/{id}` | Account details (with roles) | `200` | `404` | provided |
| PUT | `/api/account/{id}?type=` | Update a role's phone/email | `200` | `404`/`400` | provided |

**Files involved:**
- DTOs — [`dtos/user/ApiUserDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/user/ApiUserDTO.java), [`dtos/user/ApiCreateUserDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/user/ApiCreateUserDTO.java) (both already complete)
- Repository — [`repository/UserRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/UserRepository.java)
- Service — [`service/UserService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/UserService.java)
- Controller — [`controller/api/UserApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/UserApiController.java)
- Test — [`api/user/UserApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/user/UserApiControllerTest.java)

---

## Data Used or Modified

**Table:** `users`. **Entity:** `User` (frozen; implements `UserDetails`).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `name` | `name` | `name` | `String` | NOT NULL, required |
| `email` | `email` | `email` | `String` | NOT NULL, **unique**, `@Email` |
| `password` | `password` | — | `String` | NOT NULL; accepted on create/update, **never returned** |
| `createdOn`/`updateOn` | `created_on`/`update_on` | — | timestamps | internal |

**Validations & expected behavior:**
- **Format validation** (`ApiCreateUserDTO` / `@Valid`): `name`, `email`, `password` required; `email`
  valid format. A body missing any of these → `400`.
- **Email uniqueness:** the DB unique constraint on `email` rejects duplicates → surfaces as `400`
  via the `DataAccessException` handler (a service-level pre-check with `findUserByEmail` is optional).
- **Password is write-only:** `ApiUserDTO` has no password field, so responses never expose it.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`** → `created_on`/`update_on` via `NOW()`.
- **FK note:** a user referenced by a customer/courier/employee/restaurant cannot be deleted (FK) →
  `400`. The delete test creates a fresh user (no roles) so it deletes cleanly.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveUser(...)` then `getLastInsertedId()` in one
  `@Transactional` service method (same connection, correct `LAST_INSERT_ID()`).
- Reuse `mapUserToDTO(...)` and the existing JPA methods.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) — no try/catch. `@Valid` covers the missing/invalid-field cases.
- 404 detail message: `User with id {id} not found` (matches the API reference).

---

## Acceptance Criteria

- [ ] `ApiUserDTO` (`id, name, email`) and `ApiCreateUserDTO` (`name, email @Email, password`, all
      required) are present and correct (already provided — verify, don't break).
- [ ] All `UserRepository` CRUD native queries implemented with parameterized bindings and correct
      `users` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `UserService` methods implemented; create returns the DTO with a generated
      `id` (no password); get-by-id/update return `Optional.empty()` when absent; delete returns a boolean.
- [ ] `UserApiController` implements the five CRUD endpoints with correct verbs, `@Valid @RequestBody`,
      `ResponseBuilder` envelopes, and `ResourceNotFoundException`; the provided account endpoints unchanged.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404); password
      never appears in a response.
- [ ] `UserApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/users` → 200, array; `GET /{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/users` valid → 201 with matching `name`/`email`; missing fields → 400.
  - [ ] `PUT /api/users/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/users/{id}` (fresh user) → 200; unknown → 404.
- [ ] Tests use unique emails (to respect the unique constraint) and `@Transactional` rollback.
- [ ] No provided/frozen code modified; **`./mvnw test` runs the whole suite** (all workflows now compile).

---

## Notes for the AI

- The CRUD DTOs are **already implemented** — do not rewrite them; just build the repository/service/
  controller/tests around them.
- Use **unique emails** in create/update tests (e.g. a UUID) because `email` is unique; delete tests
  must create a **fresh** user (no linked role) so it can be deleted.
- Keep the diff additive: only fill the `// todo:` sections; preserve the provided account endpoints
  and service methods.
