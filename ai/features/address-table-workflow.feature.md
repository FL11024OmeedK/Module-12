# 🤖 AI_FEATURE — Address Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Address entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Address Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/address-table-workflow` (created from `dev`, merged back to `dev` when done)

---

## Feature Goal

Expose full CRUD over the `addresses` table through the JSON REST API so the mobile app can
create, read, update, and delete addresses. Addresses are a shared building block referenced by
restaurants, customers, and employees, so this workflow establishes the reference pattern for the
remaining table workflows.

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the Address vertical slice:

- **DTO** — `ApiAddressDTO` used for both request input and response output.
- **Repository** — native SQL CRUD + lookup methods on `AddressRepository`.
- **Service** — DTO-based methods on `AddressService` (create, read all, read by id, update,
  delete) plus entity↔DTO mapping.
- **Controller** — the five REST endpoints on `AddressApiController`.
- **Tests** — complete the `// todo:` MockMvc tests in `AddressApiControllerTest` (success +
  failure paths).

### Out of Scope (Excluded)

- Do **not** modify the `Address` entity/model (frozen per global spec).
- Do **not** change the already-working JPA service methods, the `findAvailableAddresses(...)`
  business method, or the `mapAddressToDTO(...)` helper already present in `AddressService` — reuse
  them.
- No pagination, filtering, search, or sorting on the list endpoint (not in the contract).
- No auth/role logic beyond the global JWT rule (all `/api/**` except `/api/auth` require a token;
  tests bypass it with `addFilters = false`).
- No changes to the Module 11 back-office `AddressController` (Thymeleaf).
- No new endpoints, fields, or DTO variants beyond what the API reference defines.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiAddressDTO`** — add fields, Lombok, JSON mapping, and validation:
   - `int id` (response only; ignored/absent on create input)
   - `String streetAddress` → `@JsonProperty("street_address")`, `@NotBlank`
   - `String city` → `@NotBlank`
   - `String postalCode` → `@JsonProperty("postal_code")`, `@NotBlank`
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.
   - Must satisfy the setters the existing `mapAddressToDTO(...)` already calls: `setId`,
     `setStreetAddress`, `setCity`, `setPostalCode`.

2. **`AddressRepository` (native SQL)** — fill the `@Query` bodies (parameterized bindings only):
   - `saveAddress(streetAddress, city, postalCode)` — `INSERT` (`@Modifying`/`@Transactional`).
   - `findAllAddresses()` → `List<Address>`.
   - `findAddressById(:addressId)` → `Optional<Address>`.
   - `updateAddress(addressId, streetAddress, city, postalCode)` — `UPDATE`
     (`@Modifying`/`@Transactional`).
   - `deleteAddressById(:addressId)` — `DELETE` (`@Modifying`/`@Transactional`).
   - `getLastInsertedId()` — returns the id of the row just inserted (used to build the create
     response, since the native `INSERT` returns void).

3. **`AddressService` (DTO methods)** — implement the five `// todo:` DTO methods:
   - Create from DTO → persist via repository → return the created DTO **with its generated id**.
   - Get all as `List<ApiAddressDTO>`.
   - Get by id as `Optional<ApiAddressDTO>` (empty when not found).
   - Update from DTO → return updated `Optional<ApiAddressDTO>` (empty when id not found).
   - Delete by id → return `boolean` (`true` if it existed and was deleted, `false` otherwise).
   - Reuse `mapAddressToDTO(...)`; keep all logic in the service.

4. **`AddressApiController`** — declare `private final AddressService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). Thin controller: delegate to the service, wrap
   success with `ResponseBuilder`, throw exceptions for errors.

5. **`AddressApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

Consumer is the mobile app / Postman (an authenticated API client).

1. **List:** client `GET /api/addresses` → service returns all addresses as DTOs → `200` with a
   `data` array.
2. **Read one:** client `GET /api/addresses/{id}` → found → `200` with the DTO; not found → `404`.
3. **Create:** client `POST /api/addresses` with a JSON body → `@Valid` passes → service inserts,
   fetches the new id, returns the created DTO → `201`. Missing/blank fields → `400`.
4. **Update:** client `PUT /api/addresses/{id}` with a JSON body → id exists → service updates and
   returns the updated DTO → `200`; id missing → `404`; invalid body → `400`.
5. **Delete:** client `DELETE /api/addresses/{id}` → existed → delete and return the deleted DTO →
   `200`; not found → `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. This is an API-only feature (no pages/components).

### Backend / API

All under base path `/api/addresses`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/addresses` | List all addresses | `200` + `data` array | — |
| GET | `/api/addresses/{id}` | Get one address | `200` + `data` object | `404` not found |
| POST | `/api/addresses` | Create address | `201` + created `data` (with `id`) | `400` invalid/missing |
| PUT | `/api/addresses/{id}` | Update address | `200` + updated `data` | `404` not found · `400` invalid |
| DELETE | `/api/addresses/{id}` | Delete address | `200` + deleted `data` | `404` not found |

**Files involved:**
- DTO — [`dtos/address/ApiAddressDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/address/ApiAddressDTO.java)
- Repository — [`repository/AddressRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/AddressRepository.java)
- Service — [`service/AddressService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/AddressService.java)
- Controller — [`controller/api/AddressApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/AddressApiController.java)
- Test — [`api/address/AddressApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/address/AddressApiControllerTest.java)

---

## Data Used or Modified

**Table:** `addresses`. **Entity:** `Address` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment (`GenerationType.IDENTITY`) |
| `streetAddress` | `street_address` | `street_address` | `String` | `NOT NULL`, `@NotBlank` |
| `city` | `city` | `city` | `String` | `NOT NULL`, `@NotBlank` |
| `postalCode` | `postal_code` | `postal_code` | `String` | `NOT NULL`, `@NotBlank` |
| `createdOn` | `created_on` | — | `LocalDateTime` | Hibernate `@CreationTimestamp`; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | Hibernate `@UpdateTimestamp`; not in API contract |

- **API exposes only** `id`, `street_address`, `city`, `postal_code`. Timestamps are internal and
  must not appear in DTO responses.
- **Validations** (on `ApiAddressDTO`, request side): all three of `street_address`, `city`,
  `postal_code` are required and non-blank. A missing/blank field → `400 Bad Request`.
- **Native SQL:** all queries use parameterized bindings (`:param` / positional) — no string
  concatenation. Match the exact column names above.

---

## Tech Constraints (Feature-Level)

- Native SQL `INSERT` returns void, so create is a two-step service operation: `saveAddress(...)`
  then `getLastInsertedId()` to obtain the generated `id` for the response DTO.
- Reuse the existing `mapAddressToDTO(...)` helper and the JPA convenience methods already in
  `AddressService`; do not duplicate mapping logic.
- Controller must use `ResponseBuilder.buildOkResponse(...)` (200) and
  `buildCreatedResponse(...)` (201); throw `ResourceNotFoundException` (404) and let the global
  handler format errors — no try/catch. Validation failures surface as `400` via `@Valid`.
- 404 detail message should read like `Address with id {id} not found` (matches the API reference).

---

## Acceptance Criteria

- [ ] `ApiAddressDTO` has `id`, `streetAddress`, `city`, `postalCode` with correct
      `@JsonProperty` snake_case mapping, `@NotBlank` on the three required fields, and Lombok
      accessors compatible with `mapAddressToDTO(...)`.
- [ ] All `AddressRepository` native queries are implemented with parameterized bindings and
      correct `addresses` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `AddressService` methods are implemented; create returns the DTO with a
      generated `id`; get-by-id/update return `Optional.empty()` when absent; delete returns a
      boolean.
- [ ] `AddressApiController` implements all five endpoints with constructor injection, correct
      HTTP verbs, `@Valid @RequestBody` where applicable, `ResponseBuilder` envelopes, and
      `ResourceNotFoundException` on missing ids.
- [ ] Responses match `Requirements/api_reference.md` exactly (field names, envelope, status
      codes: 200 / 201 / 400 / 404).
- [ ] `AddressApiControllerTest` passes with both success and failure cases:
  - [ ] `GET /api/addresses` → 200, `data` is an array.
  - [ ] `GET /api/addresses/{id}` → 200 with `data.id`; unknown id → 404.
  - [ ] `POST /api/addresses` valid → 201 with matching fields; `{}` / missing fields → 400.
  - [ ] `PUT /api/addresses/{id}` valid+known id → 200 reflecting the update; unknown id → 404.
  - [ ] `DELETE /api/addresses/{id}` known id → 200; unknown id → 404.
- [ ] Tests use seeded `DataSeeder` values (e.g. address `id = 1` exists); no hard-coded ids that
      don't match seed data; create-then-delete for the delete-success test.
- [ ] `./mvnw test` is green and no provided/frozen code was modified.

---

## Notes for the AI

- Study the already-implemented **`OrderApiController` / `OrderStatusApiController` / `RestaurantService`**
  as the reference pattern (native SQL, `Object[]` mapping, `ResponseBuilder`, exception throwing).
- The service already imports `RestaurantRepository` for `findAvailableAddresses(...)` — leave that
  untouched; it is unrelated to the CRUD DTO methods.
- Keep the diff minimal and additive: only fill the `// todo:` sections. Do not refactor working code.
- Follow TDD where practical: make the stubbed tests express the contract, then implement until green.
