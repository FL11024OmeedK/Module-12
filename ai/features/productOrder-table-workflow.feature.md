# 🤖 AI_FEATURE — ProductOrder Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the ProductOrder entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** ProductOrder Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/productOrder-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose CRUD over the `product_orders` **join table** (which product, in which order, at what quantity
and unit cost) through the JSON REST API. Two business rules apply on create/update: a product can
appear only once per order, and the product must belong to the same restaurant as the order.
Completing this workflow removes the last-but-one build blocker (its service maps to the currently
empty `ApiProductOrderDTO`).

This workflow is **partially provided**. Already implemented (do NOT modify): the business-logic
methods `createProductOrder`, `updateProductOrder`, `validateBusinessRules`, the native helper
queries (`countDuplicateProductOrder`, `findRestaurantIdByProductId`, `findRestaurantIdByOrderId`),
`deleteProductOrdersByOrderId`, and the `mapProductOrderToDTO` helper.

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs:

- **DTO** — `ApiProductOrderDTO` (`id`, `product_id`, `order_id`, `product_quantity`, `product_unit_cost`).
- **Repository** — native SQL CRUD (`saveProductOrder`, `findAllProductOrders`, `findProductOrderById`,
  `updateProductOrder`, `deleteProductOrderById`, `getLastInsertedId`). (Business helpers +
  `deleteProductOrdersByOrderId` are done.)
- **Service** — DTO-based create/read-all/read-by-id/update/delete, reusing the provided
  business-logic methods and `mapProductOrderToDTO`.
- **Controller** — the five REST endpoints on `ProductOrderApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `ProductOrder`, `Product`, or `Order` entities (frozen).
- Do **not** modify the provided business methods (`createProductOrder`, `updateProductOrder`,
  `validateBusinessRules`), the provided native helper queries, or `mapProductOrderToDTO`.
- Do **not** implement Product/Order CRUD — only reference their ids.
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/filtering; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiProductOrderDTO`** — fields the mapper already sets
   (`setId/ProductId/OrderId/ProductQuantity/ProductUnitCost`):
   - `int id`, `int productId` → `@JsonProperty("product_id")`, `int orderId` → `@JsonProperty("order_id")`,
     `Integer productQuantity` → `@JsonProperty("product_quantity")` `@NotNull @Min(1)`,
     `Integer productUnitCost` → `@JsonProperty("product_unit_cost")` `@NotNull @Min(0)`.
   - Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`ProductOrderRepository` (native SQL)** — fill the `@Query` bodies (positional where no
   `@Param`, named where present):
   - `saveProductOrder(productId, orderId, productQuantity, productUnitCost)` — `INSERT`
     (`created_on`/`update_on` via `NOW()`).
   - `findAllProductOrders()` → `List<ProductOrder>`.
   - `findProductOrderById(:productOrderId)` → `Optional<ProductOrder>`.
   - `updateProductOrder(productOrderId, productQuantity, productUnitCost)` — `UPDATE`
     (+ `update_on = NOW()`).
   - `deleteProductOrderById(:productOrderId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`ProductOrderService` (DTO methods)** — implement the `// todo:` methods, reusing provided logic:
   - **Create from DTO:** call the provided `createProductOrder(...)` (validates both business rules;
     unknown product/order fail the restaurant check) → map the saved entity → return DTO.
   - Get all as `List<ApiProductOrderDTO>` (native `findAllProductOrders`).
   - Get by id as `Optional<ApiProductOrderDTO>` (native `findProductOrderById`; empty when not found).
   - **Update from DTO:** existence pre-check → `Optional.empty()` if unknown (→ 404); otherwise call
     the provided `updateProductOrder(...)` (re-validates business rules) → return the updated DTO.
   - Delete by id → `boolean` (native `deleteProductOrderById`).
   - Reuse `mapProductOrderToDTO(...)`; keep all logic in the service.

4. **`ProductOrderApiController`** — declare `private final ProductOrderService`, constructor-inject
   it, and implement the five endpoints (see Interfaces). Thin controller: delegate, wrap success
   with `ResponseBuilder`, throw exceptions for errors.

5. **`ProductOrderApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

1. **List:** `GET /api/product-orders` → all line items as DTOs → `200` array.
2. **Read one:** `GET /api/product-orders/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/product-orders` → `@Valid` passes → business rules validated (product &
   order exist, same restaurant, product not already in the order) → insert → `201`. Rule/existence
   violation → `400`.
4. **Update:** `PUT /api/product-orders/{id}` → id exists → business rules re-validated → update →
   `200`; id missing → `404`; rule violation → `400`.
5. **Delete:** `DELETE /api/product-orders/{id}` → existed → delete, return deleted DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/product-orders`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error |
|--------|----------|---------|---------|-------|
| GET | `/api/product-orders` | List all line items | `200` + array | — |
| GET | `/api/product-orders/{id}` | Get one line item | `200` | `404` |
| POST | `/api/product-orders` | Create line item | `201` (with `id`) | `400` invalid/rule violation |
| PUT | `/api/product-orders/{id}` | Update line item | `200` | `404` · `400` rule violation |
| DELETE | `/api/product-orders/{id}` | Delete line item | `200` (deleted data) | `404` |

**Files involved:**
- DTO — [`dtos/productOrder/ApiProductOrderDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/productOrder/ApiProductOrderDTO.java)
- Repository — [`repository/ProductOrderRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/ProductOrderRepository.java)
- Service — [`service/ProductOrderService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/ProductOrderService.java)
- Controller — [`controller/api/ProductOrderApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/ProductOrderApiController.java)
- Test — [`api/productOrder/ProductOrderApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/productOrder/ProductOrderApiControllerTest.java)

---

## Data Used or Modified

**Table:** `product_orders` (unique `(product_id, order_id)`). **Entity:** `ProductOrder` (frozen).

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `product` (→ id) | `product_id` | `product_id` | `int` | FK → `products`, NOT NULL |
| `order` (→ id) | `order_id` | `order_id` | `int` | FK → `orders`, NOT NULL |
| `productQuantity` | `product_quantity` | `product_quantity` | `Integer` | NOT NULL, `@Min(1)` |
| `productUnitCost` | `product_unit_cost` | `product_unit_cost` | `Integer` | NOT NULL, `@Min(0)` |
| `createdOn` | `created_on` | — | `LocalDateTime` | internal; not in API contract |
| `updateOn` | `update_on` | — | `LocalDateTime` | internal; not in API contract |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `product_quantity` required `>= 1`; `product_unit_cost`
  required `>= 0`.
- **Business rules** (provided `validateBusinessRules`, throws `IllegalArgumentException` → `400`):
  (1) a product cannot appear twice in the same order; (2) the product and order must belong to the
  same restaurant. Unknown product/order ids fail rule (2) (restaurant lookup returns null) → `400`.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`**, so `created_on`/`update_on` are
  set via `NOW()` on insert/update.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- The provided `createProductOrder`/`updateProductOrder` encapsulate the business-rule validation —
  the DTO create/update **reuse** them rather than re-implementing the rules. The DTO update adds an
  existence pre-check so an unknown id returns `404` (not the provided methods' `400`).
- Reads and delete use the native `findAllProductOrders` / `findProductOrderById` /
  `deleteProductOrderById` queries; `getLastInsertedId` supports any native create path.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) — no try/catch. `@Valid` covers the quantity/cost format rules;
  `IllegalArgumentException` from the business rules maps to `400` via the global handler.
- 404 detail message: `Product order with id {id} not found` (matches the API reference).

---

## Acceptance Criteria

- [ ] `ApiProductOrderDTO` has `id, productId, orderId, productQuantity, productUnitCost` with correct
      `@JsonProperty` snake_case mapping, `@NotNull`/`@Min` on quantity & cost, and Lombok accessors
      compatible with `mapProductOrderToDTO(...)`.
- [ ] All `ProductOrderRepository` CRUD native queries implemented with parameterized bindings and
      correct `product_orders` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] The DTO-based `ProductOrderService` methods implemented; create/update reuse the provided
      validated business methods; update returns `Optional.empty()` for an unknown id; delete returns
      a boolean.
- [ ] `ProductOrderApiController` implements all five endpoints with constructor injection, correct
      verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `ProductOrderApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/product-orders` → 200, array; `GET /{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/product-orders` valid (product & order of the same restaurant) → 201 with matching
        fields; non-existent product/order → 400.
  - [ ] `PUT /api/product-orders/{id}` valid+known → 200 reflecting the update; unknown → 404.
  - [ ] `DELETE /api/product-orders/{id}` known → 200; unknown → 404.
- [ ] Tests build a valid product + order of the **same restaurant** (fresh, so no duplicate), use
      `@Transactional` rollback, and don't hard-code non-seeded ids.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Both business rules make the create test setup specific:** the product and order must share a
  restaurant, and the product must not already be in the order. The simplest deterministic setup is
  to persist a **fresh product and a fresh order for the same seeded restaurant** (via their JPA
  repositories) in the test, then create the product_order for that pair.
- Reuse the provided `createProductOrder`/`updateProductOrder` — they already throw
  `IllegalArgumentException` (→ 400) for rule violations; just add the 404 existence check on update.
- Keep the diff additive: only fill the `// todo:` sections; preserve all provided reference code.
