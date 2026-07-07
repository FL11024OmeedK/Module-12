# 🤖 AI_FEATURE — Product Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Product entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Product Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/product-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose full CRUD over the `products` table through the JSON REST API so the mobile app can manage a
restaurant's menu items. A product belongs to a **restaurant** and carries a `name`, optional
`description`, and integer `cost`. The list endpoint can be **optionally filtered by restaurant**.
Completing this workflow also removes one of the last build blockers (its service maps to the
currently-empty `ApiProductDTO`).

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs for the Product vertical slice:

- **DTOs** — `ApiProductDTO` (response) and `ApiCreateProductDTO` (create/update request).
- **Repository** — native SQL CRUD + `getLastInsertedId`. (`findProductsByRestaurantId` is done.)
- **Service** — DTO-based create, read-all, read-by-restaurant, read-by-id, update, delete, reusing
  the provided `mapProductToDTO` helper.
- **Controller** — the five REST endpoints on `ProductApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `Product` or `Restaurant` entities (frozen).
- Do **not** implement Restaurant CRUD — only reference `restaurant_id`.
- Do **not** touch `ApiProductForOrderApiDTO` (used by the Order workflow) — this feature only
  concerns `ApiProductDTO` / `ApiCreateProductDTO`.
- Do **not** change the already-working JPA methods, the `findProductsByRestaurantId` wiring, or the
  `mapProductToDTO(...)` helper — reuse them.
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/sorting; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiProductDTO`** (response) — fields the mapper already sets
   (`setId/RestaurantId/Name/Description/Cost`):
   - `int id`, `int restaurantId` → `@JsonProperty("restaurant_id")`, `String name`,
     `String description`, `int cost`. Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`ApiCreateProductDTO`** (create + update request):
   - `int restaurantId` → `@JsonProperty("restaurant_id")`
   - `String name` → `@NotBlank`
   - `String description` (optional)
   - `Integer cost` → `@NotNull`, `@Min(0)`
   - Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

3. **`ProductRepository` (native SQL)** — fill the `@Query` bodies (positional where no `@Param`,
   named where present):
   - `saveProduct(restaurantId, name, description, cost)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllProducts()` → `List<Product>`.
   - `findProductById(:productId)` → `Optional<Product>`.
   - `updateProduct(productId, name, description, cost)` — `UPDATE` (+ `update_on = NOW()`). Does NOT
     touch `restaurant_id`.
   - `deleteProductById(:productId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

4. **`ProductService` (DTO methods)** — implement the `// todo:` methods:
   - **Create from DTO:** validate the referenced `restaurant_id` exists → **`400`** if not; insert;
     fetch new id; return created DTO.
   - Get all as `List<ApiProductDTO>`.
   - Get by restaurant as `List<ApiProductDTO>` — validate the restaurant exists → **`404`** if not.
   - Get by id as `Optional<ApiProductDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiProductDTO>` (empty when id not found); `restaurant_id` immutable.
   - Delete by id → `boolean`.
   - Reuse `mapProductToDTO(...)`; keep all logic in the service.

5. **`ProductApiController`** — declare `private final ProductService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). `GET /api/products` takes an optional `restaurant`
   query param. Thin controller: delegate, wrap success with `ResponseBuilder`, throw exceptions.

6. **`ProductApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

1. **List:** `GET /api/products` → all products; `GET /api/products?restaurant={id}` → that
   restaurant's products (unknown restaurant → `404`).
2. **Read one:** `GET /api/products/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/products` → `@Valid` passes → restaurant must exist (`400` if not) →
   insert → `201`. Blank `name` / missing/negative `cost` → `400`.
4. **Update:** `PUT /api/products/{id}` → id exists → update `name`, `description`, `cost` → `200`;
   id missing → `404`.
5. **Delete:** `DELETE /api/products/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/products`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/products` | List all products | `200` + array | — |
| GET | `/api/products?restaurant={id}` | List a restaurant's products | `200` + array | `404` unknown restaurant |
| GET | `/api/products/{id}` | Get one product | `200` | `404` |
| POST | `/api/products` | Create product | `201` (with `id`) | `400` invalid/missing/unknown restaurant |
| PUT | `/api/products/{id}` | Update product | `200` | `404` · `400` invalid |
| DELETE | `/api/products/{id}` | Delete product | `200` (deleted data) | `404` |

> **Contract note:** the API reference shows the *list* items trimmed to `{id, name, cost}`, but the
> provided `mapProductToDTO` produces the full `ApiProductDTO` (`id`, `name`, `description`, `cost`,
> `restaurant_id`). This feature returns the full DTO (a superset) consistently for list/read/create/update.

**Files involved:**
- DTOs — [`dtos/product/ApiProductDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/product/ApiProductDTO.java), [`dtos/product/ApiCreateProductDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/product/ApiCreateProductDTO.java)
- Repository — [`repository/ProductRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/ProductRepository.java)
- Service — [`service/ProductService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/ProductService.java)
- Controller — [`controller/api/ProductApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/ProductApiController.java)
- Test — [`api/product/ProductApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/product/ProductApiControllerTest.java)

---

## Data Used or Modified

**Table:** `products`. **Entity:** `Product` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `restaurant` (→ id) | `restaurant_id` | `restaurant_id` | `int` | FK → `restaurants`, NOT NULL |
| `name` | `name` | `name` | `String` | NOT NULL, `@NotBlank` |
| `description` | `description` | `description` | `String` | nullable |
| `cost` | `cost` | `cost` | `Integer` | NOT NULL, `@Min(0)` (integer cents) |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `name` required (not blank); `cost` required and `>= 0`;
  `description` optional. A missing name / missing-or-negative cost → `400`.
- **Referential validation** (service): on **create**, `restaurant_id` must reference an existing
  restaurant → `400` if not. On the **list filter**, an unknown `restaurant` → `404`.
- **`restaurant_id` is immutable on update** — `updateProduct` changes only `name`, `description`,
  `cost`; the response echoes the stored `restaurant_id`.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`**, so `created_on`/`update_on` are
  set via `NOW()` on insert/update.
- **FK note:** deleting a product referenced by a `product_order` violates the FK → `400`. The delete
  test uses a freshly created product, so it is unreferenced and deletes cleanly.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveProduct(...)` then `getLastInsertedId()`
  in one `@Transactional` service method (same connection, correct `LAST_INSERT_ID()`).
- Reuse `mapProductToDTO(...)` and the JPA/`findProductsByRestaurantId` methods already in the service.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) and `BadRequestException` (400) — no try/catch. `@Valid` covers
  format errors.
- 404 detail messages: `Product with id {id} not found`, `Restaurant with id {id} not found`.

---

## Acceptance Criteria

- [ ] `ApiProductDTO` (`id, restaurantId, name, description, cost`) and `ApiCreateProductDTO`
      (`restaurantId, name @NotBlank, description, cost @NotNull @Min(0)`) have correct
      `@JsonProperty` mapping and Lombok accessors compatible with `mapProductToDTO(...)`.
- [ ] All `ProductRepository` native queries implemented with parameterized bindings and correct
      `products` columns; update leaves `restaurant_id` untouched; write queries carry `@Modifying`
      + `@Transactional`.
- [ ] The DTO-based `ProductService` methods implemented; create validates the restaurant (400) and
      returns the DTO with a generated `id`; the restaurant filter validates (404); get-by-id/update
      return `Optional.empty()` when absent; delete returns a boolean.
- [ ] `ProductApiController` implements all five endpoints with constructor injection, correct verbs,
      the optional `restaurant` query param, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and
      `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `ProductApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/products` → 200, array.
  - [ ] `GET /api/products/{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/products` valid → 201 with matching fields; missing name → 400.
  - [ ] `PUT /api/products/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/products/{id}` known → 200; unknown → 404.
- [ ] Tests build a valid `restaurant_id` from seeded data, use `@Transactional` rollback, and don't
      hard-code non-seeded ids.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Product depends on a Restaurant row existing.** Tests must reference a valid seeded
  `restaurant_id` (`restaurantRepository.findAll().get(0).getId()`).
- Mirror the completed **Address** workflow for the plain CRUD shape, plus the create-time FK
  validation seen in **Courier/Customer**.
- The create/update request DTO is the **same** `ApiCreateProductDTO`; update ignores `restaurant_id`.
- Keep the diff additive: only fill the `// todo:` sections; do not refactor working code.
