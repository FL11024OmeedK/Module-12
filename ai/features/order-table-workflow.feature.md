# 🤖 AI_FEATURE — Order Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Order entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Order Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/order-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Complete CRUD + workflow operations over the `orders` table (and their `product_orders` line
items) through the JSON REST API. An order links a **customer**, a **restaurant**, an
**order status**, an optional **courier**, an optional **restaurant rating**, and a list of ordered
**products** (each with quantity and unit cost). This is the most complex slice: creating an order
also creates its product line items and computes a total cost.

This workflow is **partially provided**. Already implemented (do NOT modify): `getOrdersByTypeAndId`,
`assignCourier`, `updateRating`, the `mapOrderToDTO` helper, `ApiOrderDTO`,
`ApiProductForOrderApiDTO`, and the `updateOrderStatus` native query.

---

## Feature Scope

### In Scope (Included)

Complete the remaining `// todo:` stubs:

- **DTOs** — `ApiCreateOrderDTO` (with a `ProductItem` inner class) and `ApiUpdateOrderDTO`.
- **Repository** — native SQL for `saveOrder`, `findAllOrders`, `findOrderById`,
  `findOrdersByRestaurantId`, `findOrdersByCustomerId`, `findOrdersByCourierId`, `updateOrder`,
  `deleteOrderById`, `getLastInsertedId`. (`updateOrderStatus` is already done.)
- **Service** — DTO-based `createOrder`, `getOrderByIdAsDto`, `updateOrder`, `deleteOrder`.
- **Controller** — `POST /api/orders`, `PUT /api/orders/{id}`, `DELETE /api/orders/{id}`.
- **Tests** — complete the `// todo:` MockMvc tests for create/update/delete (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `Order`, `ProductOrder`, `Product`, `Customer`, `Restaurant`, `Courier`, or
  `OrderStatus` entities (frozen).
- Do **not** modify the provided reference code (`getOrdersByTypeAndId`, `assignCourier`,
  `updateRating`, `mapOrderToDTO`, `ApiOrderDTO`, `updateOrderStatus`) or their given tests
  (`testGetOrders_*`).
- Do **not** implement the **ProductOrder** workflow's own repository/service/controller — that is
  a separate feature. Order creation persists line items via the inherited JPA `save()` on
  `ProductOrderRepository` (not its stub native queries), plus JPA cascade for deletion.
- Do **not** implement Product/Customer/Restaurant/OrderStatus CRUD — only reference their ids.

---

## Sub-Requirements (Feature Breakdown)

1. **`ApiCreateOrderDTO`** — request body for create:
   - `int restaurantId` → `@JsonProperty("restaurant_id")`
   - `int customerId` → `@JsonProperty("customer_id")`
   - `List<ProductItem> products` → `@NotEmpty`
   - static inner class `ProductItem { int id; int quantity; }` (Lombok `@Getter @Setter`).
   - Lombok `@Getter @Setter`.

2. **`ApiUpdateOrderDTO`** — request body for update:
   - `int customerId` → `@JsonProperty("customer_id")`
   - `int restaurantId` → `@JsonProperty("restaurant_id")`
   - `Integer courierId` → `@JsonProperty("courier_id")` (optional/nullable)
   - Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

3. **`OrderRepository` (native SQL)** — fill the `@Query` bodies (positional where no `@Param`,
   named where present):
   - `saveOrder(restaurantId, customerId, orderStatusId)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllOrders()`, `findOrderById(:orderId)`, `findOrdersByRestaurantId(:restaurantId)`,
     `findOrdersByCustomerId(:customerId)`, `findOrdersByCourierId(:courierId)` — `SELECT *` filters.
     (The `findOrdersBy*` queries are required by the already-working `GET /api/orders`.)
   - `updateOrder(orderId, orderStatusId, restaurantRating)` — `UPDATE order_status_id`,
     `restaurant_rating` (+ `update_on = NOW()`).
   - `deleteOrderById(:orderId)`, `getLastInsertedId()`.

4. **`OrderService` (DTO methods)**:
   - **`createOrder(ApiCreateOrderDTO)`** → `ApiOrderDTO`. Business logic:
     validate restaurant & customer exist; products non-empty; **each product belongs to the
     restaurant**; **no duplicate product in one order**; insert order with status `pending`
     (native `saveOrder` + `getLastInsertedId`); insert one `product_order` per item using the
     product's `cost` as unit cost (JPA `save`); return the created order as a DTO.
   - **`getOrderByIdAsDto(int)`** → `Optional<ApiOrderDTO>`.
   - **`updateOrder(int id, ApiUpdateOrderDTO)`** → `Optional<ApiOrderDTO>`. Reassign
     customer/restaurant/(optional courier) by id; `Optional.empty()` when the order id is unknown.
   - **`deleteOrder(int id)`** → `boolean` (JPA `deleteById` cascades to `product_orders`).
   - Reuse `mapOrderToDTO(...)`; keep all logic in the service; `@Transactional` on create/update/delete.

5. **`OrderApiController`** — implement the three endpoints, delegating to the service, wrapping
   success in `ResponseBuilder`, and throwing `ResourceNotFoundException` (404) / `BadRequestException` (400).

6. **`OrderApiControllerTest`** — complete the create/update/delete tests (success + failure).

---

## User Flow / Logic (High Level)

1. **List by type:** `GET /api/orders?type={customer|restaurant|courier}&id={id}` → array (provided).
2. **Create:** `POST /api/orders` with restaurant, customer, and products → validate → insert order
   (`pending`) + line items → `201` with the full order (products + total cost).
3. **Update:** `PUT /api/orders/{id}` → reassign customer/restaurant/courier → `200`; unknown id → `404`.
4. **Delete:** `DELETE /api/orders/{id}` → delete order + its line items → `200`; unknown id → `404`.
5. **Assign courier / rating:** `PUT /api/order/{id}/courier`, `PUT /api/order/{id}/rating` (provided).

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Success envelope for create/update/delete: `{ "message": "Success", "data": ... }`. (The provided
`GET /api/orders` returns a raw array — left as-is.)

| Method | Endpoint | Purpose | Success | Error | Status |
|--------|----------|---------|---------|-------|--------|
| GET | `/api/orders?type=&id=` | List orders by owner | raw array | `400` invalid type | provided |
| POST | `/api/orders` | Create order + line items | `201` + order DTO | `400` invalid/missing | **new** |
| PUT | `/api/orders/{id}` | Reassign customer/restaurant/courier | `200` | `404` · `400` | **new** |
| DELETE | `/api/orders/{id}` | Delete order (+line items) | `200` (deleted data) | `404` | **new** |
| PUT | `/api/order/{id}/courier` | Assign courier | `200` | `404` | provided |
| PUT | `/api/order/{id}/rating` | Set restaurant rating (1–5) | `200` | `404` | provided |

> **Contract note:** the API reference shows `POST /api/orders` returning `200`; the test stub and
> project-wide create convention use **`201 Created`** with the standard envelope. This spec follows
> the test/convention (201 + `ResponseBuilder.buildCreatedResponse`).

**Files involved:**
- DTOs — [`dtos/order/ApiCreateOrderDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/order/ApiCreateOrderDTO.java), [`dtos/order/ApiUpdateOrderDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/order/ApiUpdateOrderDTO.java)
- Repository — [`repository/OrderRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/OrderRepository.java)
- Service — [`service/OrderService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/OrderService.java)
- Controller — [`controller/api/OrderApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/OrderApiController.java)
- Test — [`api/order/OrderApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/order/OrderApiControllerTest.java)

---

## Data Used or Modified

**Tables:** `orders` (+ `product_orders` line items). **Entities:** `Order`, `ProductOrder` (frozen).

`orders`: `id`, `restaurant_id` (FK, NOT NULL), `customer_id` (FK, NOT NULL), `courier_id` (FK,
nullable), `order_status_id` (FK, NOT NULL), `restaurant_rating` (1–5, nullable), `created_on`,
`update_on`. `product_orders`: `id`, `product_id`, `order_id`, `product_quantity` (≥1),
`product_unit_cost` (≥0), unique `(product_id, order_id)`.

**Response shape** (`ApiOrderDTO`, provided): `id`, `customer_id`, `customer_name`,
`customer_address`, `restaurant_id`, `restaurant_name`, `restaurant_address`, `courier_id`,
`courier_name`, `status`, `products[]` (`product_id`, `product_name`, `quantity`, `unit_cost`,
`total_cost`), `total_cost`, `created_on`. Per-product `total_cost = quantity × unit_cost`;
order `total_cost` = sum of those.

**Validations & expected behavior:**
- **Create:** `restaurant_id` & `customer_id` must exist; `products` non-empty (`@NotEmpty`); every
  product must belong to that restaurant; the same product cannot appear twice. Any violation →
  `400` (`error: "Bad Request"`). New orders start with status `pending`; `courier` is unassigned.
- **Update:** unknown order id → `404`; referenced customer/restaurant (and courier if provided)
  must exist → else `400`.
- **Delete:** deleting an order also deletes its `product_orders` (JPA cascade / orphanRemoval).
- Native SQL uses parameterized bindings; `created_on`/`update_on` set via `NOW()`.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create uses `saveOrder(...)` + `getLastInsertedId()` in one
  `@Transactional` service method (same connection → correct `LAST_INSERT_ID()`).
- Line items are persisted with the **inherited** `ProductOrderRepository.save(...)` (JPA) — the
  ProductOrder native stubs belong to their own workflow and stay untouched.
- Delete uses JPA `orderRepository.deleteById(id)` so the cascade removes `product_orders` safely
  (a bare native `DELETE FROM orders` would violate the FK while line items exist).
- Reuse `mapOrderToDTO(...)`. Lazy relations load under Spring's open-session-in-view (as the
  provided `getOrdersByTypeAndId` already relies on).
- Controller: `ResponseBuilder` for success; throw `ResourceNotFoundException`/`BadRequestException`
  — no try/catch. 404 detail: `Order with id {id} not found`.

---

## Acceptance Criteria

- [ ] `ApiCreateOrderDTO` (with `ProductItem`) and `ApiUpdateOrderDTO` have correct fields,
      `@JsonProperty` mapping, and `@NotEmpty` on products.
- [ ] All `OrderRepository` native queries implemented with parameterized bindings and correct
      `orders` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] `createOrder` validates (existence, non-empty products, product-belongs-to-restaurant,
      no-duplicate-product), inserts the order as `pending`, inserts line items with the product's
      cost as unit cost, and returns the full order DTO with computed `total_cost`.
- [ ] `updateOrder` reassigns customer/restaurant/courier and returns `Optional.empty()` for an
      unknown id; `deleteOrder` returns a boolean and removes line items.
- [ ] `OrderApiController` implements POST (201) / PUT (200/404) / DELETE (200/404) using
      `ResponseBuilder` and exceptions; provided endpoints unchanged.
- [ ] `OrderApiControllerTest` passes create/update/delete success + failure:
  - [ ] `POST /api/orders` valid → 201 with `data.id`, `data.status = "pending"`, `data.products`
        array, numeric `data.total_cost`; invalid (unknown restaurant) → 400 `error: "Bad Request"`.
  - [ ] `PUT /api/orders/{id}` valid+known → 200 with `data.id`; unknown → 404.
  - [ ] `DELETE /api/orders/{id}` known → 200; unknown → 404.
- [ ] Tests build valid references from seeded data (a product + its restaurant + a customer +,
      for update, a courier), use `@Transactional` rollback, and don't hard-code non-seeded ids.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- **Order creation is the crux.** Steps: validate → native `saveOrder(restaurantId, customerId,
  pendingStatusId)` → `getLastInsertedId()` → for each item, JPA-save a `ProductOrder` (unit cost =
  `product.getCost()`) referencing the new order → flush/clear → re-fetch via `findOrderById` →
  `mapOrderToDTO`.
- Find the `pending` status by name from `OrderStatusRepository.findAll()` (seeded as id 1) rather
  than hard-coding the id.
- The provided `getOrdersByTypeAndId` depends on `findOrdersByRestaurantId/CustomerId/CourierId` —
  those native queries **must** be implemented for the existing GET to work.
- Keep the diff additive: only fill `// todo:` sections; preserve all provided reference code.
