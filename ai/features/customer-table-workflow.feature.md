# 🤖 AI_FEATURE — Customer Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Customer entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Customer Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/customer-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose full CRUD over the `customers` table through the JSON REST API so the mobile app can manage
customers. A customer links a **user** (login identity) and an **address**, and carries contact
info (`phone`, `email`) and an `active` flag. The workflow also provides a **lookup by user id**
used to resolve a user's customer role (the auth flow's `customer_id`). It is structurally the same
as the Courier workflow (already on `dev`), minus the courier-status relationship.

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the Customer vertical slice:

- **DTO** — `ApiCustomerDTO` for request input and response output.
- **Repository** — native SQL CRUD + `findCustomerByUserId` lookup + `getLastInsertedId`.
- **Service** — DTO-based methods (create, read all, read by id, update, delete) plus the existing
  `findCustomerByUserId` and `mapCustomerToDTO` helper.
- **Controller** — the five REST endpoints on `CustomerApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure) in `CustomerApiControllerTest`.

### Out of Scope (Excluded)

- Do **not** modify the `Customer`, `User`, `Address`, or `Order` entities (frozen).
- Do **not** implement User or Address CRUD here — those are their own workflows. This feature only
  **references** their ids (FKs) and assumes they already exist.
- Do **not** expose or manage the customer's `orders` relationship — it is not part of the API contract.
- Do **not** change the already-working JPA methods, the `findCustomerByUserId` service wiring, or
  the `mapCustomerToDTO(...)` helper — reuse them.
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/filtering; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiCustomerDTO`** — fields the mapper already sets (`setId/UserId/AddressId/Phone/Email/Active`):
   - `int id` → `id` (response only)
   - `int userId` → `@JsonProperty("user_id")`
   - `int addressId` → `@JsonProperty("address_id")`
   - `String phone` → `phone`, `@NotBlank`
   - `String email` → `email`, `@Email` (optional)
   - `Boolean active` → `active`
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`CustomerRepository` (native SQL)** — fill the `@Query` bodies (parameterized bindings;
   **positional** where no `@Param`, **named** where present):
   - `saveCustomer(userId, addressId, phone, email)` — `INSERT`. No `active` param → set
     `active = true` in the SQL; set `created_on`/`update_on` to `NOW()`.
   - `findAllCustomers()` → `List<Customer>`.
   - `findCustomerById(:customerId)` → `Optional<Customer>`.
   - `findCustomerByUserId(:userId)` → `Optional<Customer>` (lookup).
   - `updateCustomer(customerId, phone, email, active)` — `UPDATE`. Updates only `phone`, `email`,
     `active` (+ `update_on = NOW()`); does NOT touch `user_id` or `address_id`.
   - `deleteCustomerById(:customerId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`CustomerService` (DTO methods)** — implement the five `// todo:` DTO methods:
   - **Create from DTO:** validate the referenced `user_id` and `address_id` exist and that the
     user does not already have a customer (one customer per user); insert; fetch new id; return
     created DTO (with `active = true`).
   - Get all as `List<ApiCustomerDTO>`.
   - Get by id as `Optional<ApiCustomerDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiCustomerDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Reuse `mapCustomerToDTO(...)`; keep all logic in the service.

4. **`CustomerApiController`** — declare `private final CustomerService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). Thin controller: delegate, wrap success with
   `ResponseBuilder`, throw exceptions for errors.

5. **`CustomerApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

Consumer is the mobile app / Postman (an authenticated API client).

1. **List:** `GET /api/customers` → all customers as DTOs → `200` array.
2. **Read one:** `GET /api/customers/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/customers` → `@Valid` passes → service checks the two FKs exist and the
   user has no customer yet → insert (`active = true`) → fetch id → `201`. Blank `phone`/invalid
   `email` → `400`; unknown/duplicate FK → `400`.
4. **Update:** `PUT /api/customers/{id}` → id exists → update `phone`, `email`, `active` → `200`;
   id missing → `404`; invalid body → `400`.
5. **Delete:** `DELETE /api/customers/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/customers`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/customers` | List all customers | `200` + array | — |
| GET | `/api/customers/{id}` | Get one customer | `200` | `404` |
| POST | `/api/customers` | Create customer | `201` (with `id`) | `400` invalid/missing/duplicate |
| PUT | `/api/customers/{id}` | Update customer | `200` | `404` · `400` invalid |
| DELETE | `/api/customers/{id}` | Delete customer | `200` (deleted data) | `404` |

**Files involved:**
- DTO — [`dtos/customer/ApiCustomerDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/customer/ApiCustomerDTO.java)
- Repository — [`repository/CustomerRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/CustomerRepository.java)
- Service — [`service/CustomerService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/CustomerService.java)
- Controller — [`controller/api/CustomerApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/CustomerApiController.java)
- Test — [`api/customer/CustomerApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/customer/CustomerApiControllerTest.java)

---

## Data Used or Modified

**Table:** `customers`. **Entity:** `Customer` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `user` (→ id) | `user_id` | `user_id` | `int` | FK → `users`, **unique** (one customer per user), NOT NULL |
| `address` (→ id) | `address_id` | `address_id` | `int` | FK → `addresses`, NOT NULL |
| `orders` | — | — | `List<Order>` | inverse relation; **not** in the API contract |
| `phone` | `phone` | `phone` | `String` | NOT NULL, `@NotBlank` |
| `email` | `email` | `email` | `String` | nullable, `@Email` when present |
| `active` | `active` | `active` | `Boolean` | NOT NULL, DB default `true` |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `phone` required (not blank); `email` valid format if
  provided. A `{}` body → `400` (blank `phone`).
- **Referential validation** (service): `user_id` and `address_id` must reference existing rows; a
  user may have at most one customer. Failures → `400` (the DB unique constraint on `user_id` also
  surfaces as `400` via the `DataAccessException` handler).
- **`active` on create defaults to `true`** — the `saveCustomer` native query has no `active`
  parameter and sets `active = true`. `active` is mutable through **update**.
- **`user_id` and `address_id` are immutable on update** — `updateCustomer` changes only `phone`,
  `email`, `active`. Those two fields in the PUT body are ignored; the response echoes their stored
  values.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`**, so `created_on`/`update_on` are
  set via `NOW()` on insert/update.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveCustomer(...)` then `getLastInsertedId()`
  in one `@Transactional` service method (same connection, correct `LAST_INSERT_ID()`).
- Reuse `mapCustomerToDTO(...)` and the JPA/`findCustomerByUserId` methods already in the service.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) and `BadRequestException` (400) — no try/catch. `@Valid` covers
  format errors.
- 404 detail message: `Customer with id {id} not found` (matches the API reference).
- DELETE returns the deleted customer DTO (the API reference's delete example shows a subset of
  fields; returning the full DTO is an acceptable superset).

---

## Acceptance Criteria

- [ ] `ApiCustomerDTO` has `id, userId, addressId, phone, email, active` with correct
      `@JsonProperty` snake_case mapping, `@NotBlank` on `phone`, `@Email` on `email`, and Lombok
      accessors compatible with `mapCustomerToDTO(...)`.
- [ ] All `CustomerRepository` native queries implemented with parameterized bindings and correct
      `customers` columns; create sets `active = true`; update leaves `user_id`/`address_id`
      untouched; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `CustomerService` methods implemented; create validates FK existence +
      one-customer-per-user and returns the DTO with a generated `id`; get-by-id/update return
      `Optional.empty()` when absent; delete returns a boolean.
- [ ] `CustomerApiController` implements all five endpoints with constructor injection, correct HTTP
      verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `CustomerApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/customers` → 200, array.
  - [ ] `GET /api/customers/{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/customers` valid → 201 with matching fields; `{}` → 400.
  - [ ] `PUT /api/customers/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/customers/{id}` known → 200; unknown → 404.
- [ ] Tests build valid FKs (create a fresh user for `user_id`; use a seeded `address_id`), use
      `@Transactional` rollback, and avoid hard-coded ids that don't match seeded/created data.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Customer depends on User and Address rows existing.** Tests must create/seed a valid `user_id`
  (one per customer) and reference a valid `address_id`. The User API is not built yet, so create a
  fresh `User` via `UserRepository` in the test (same approach used by the Courier test).
- Watch the two asymmetries (identical to Courier): **create ignores `active` (defaults true)** and
  **update ignores `user_id`/`address_id`**. Match the provided repository method signatures exactly.
- Mirror the completed **Courier** workflow on `dev` — it is the closest reference pattern.
- Keep the diff minimal and additive: only fill the `// todo:` sections; do not refactor working code.
