# 🤖 AI_FEATURE — OrderStatus Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the OrderStatus entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** OrderStatus Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/orderStatus-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose full CRUD over the `order_statuses` table through the JSON REST API. Order statuses are a
small **reference/lookup table** (seeded: `pending`, `in progress`, `delivered`) that orders point
to via `order_status_id`. It is the same simple `id`/`name` pattern as CourierStatus, with one
**already-provided** custom endpoint (`POST /api/order/{order_id}/status`) that changes an order's
status by name.

This workflow is **partially provided**. Already implemented (do NOT modify): `ApiOrderStatusDTO`
(`{status}`), the custom `updateOrderStatus` controller endpoint, the `updateOrderStatusForOrder`
service method, the `findOrderStatusByName` native query, and the `mapOrderStatusToDTO` helper.

---

## Feature Scope

### In Scope (Included)

Complete the remaining `// todo:` stubs for the CRUD side:

- **DTO** — `ApiOrderStatusCrudDTO` (`id`, `name`) for CRUD request input and response output.
- **Repository** — native SQL `saveOrderStatus`, `findAllOrderStatuses`, `findOrderStatusById`,
  `updateOrderStatus`, `deleteOrderStatusById`, `getLastInsertedId`. (`findOrderStatusByName` is done.)
- **Service** — DTO-based create/read-all/read-by-id/update/delete, reusing `mapOrderStatusToDTO`.
- **Controller** — the five CRUD endpoints on `OrderStatusApiController`.
- **Tests** — complete the `// todo:` MockMvc tests: the two custom-endpoint tests
  (`testUpdateOrderStatus_*`) and the CRUD create/update/delete tests.

### Out of Scope (Excluded)

- Do **not** modify the `OrderStatus` entity (frozen).
- Do **not** modify the provided reference code: `ApiOrderStatusDTO`, the custom
  `POST /api/order/{order_id}/status` endpoint, `updateOrderStatusForOrder`, `findOrderStatusByName`,
  `mapOrderStatusToDTO`.
- No enforcement of a fixed status vocabulary for CRUD (any non-blank `name` is allowed); no
  uniqueness rule on `name`.
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No pagination/filtering; no endpoints or fields beyond the API reference.

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiOrderStatusCrudDTO`** — fields the mapper already sets (`setId`, `setName`):
   - `int id` → `id` (response only)
   - `String name` → `name`, `@NotBlank`
   - Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

2. **`OrderStatusRepository` (native SQL)** — fill the `@Query` bodies (positional where no
   `@Param`, named where present):
   - `saveOrderStatus(name)` — `INSERT` (`created_on`/`update_on` via `NOW()`).
   - `findAllOrderStatuses()` → `List<OrderStatus>`.
   - `findOrderStatusById(:orderStatusId)` → `Optional<OrderStatus>`.
   - `updateOrderStatus(orderStatusId, name)` — `UPDATE` (+ `update_on = NOW()`).
   - `deleteOrderStatusById(:orderStatusId)` — `DELETE`.
   - `getLastInsertedId()` — `SELECT LAST_INSERT_ID()`.

3. **`OrderStatusService` (DTO methods)** — implement the CRUD `// todo:` methods:
   - Create from DTO → insert → fetch new id → return created DTO (with `id`).
   - Get all as `List<ApiOrderStatusCrudDTO>`.
   - Get by id as `Optional<ApiOrderStatusCrudDTO>` (empty when not found).
   - Update from DTO → `Optional<ApiOrderStatusCrudDTO>` (empty when id not found).
   - Delete by id → `boolean`.
   - Reuse `mapOrderStatusToDTO(...)`; keep all logic in the service.

4. **`OrderStatusApiController`** — implement the five CRUD endpoints under `/api/order-statuses`,
   delegating to the service, wrapping success with `ResponseBuilder`, and throwing
   `ResourceNotFoundException` (404). Keep the provided custom `POST /api/order/{order_id}/status`.

5. **`OrderStatusApiControllerTest`** — complete the stubbed tests (custom endpoint + CRUD).

---

## User Flow / Logic (High Level)

1. **List:** `GET /api/order-statuses` → all statuses as DTOs → `200` array.
2. **Read one:** `GET /api/order-statuses/{id}` → found → `200`; not found → `404`.
3. **Create:** `POST /api/order-statuses` with `{ "name": "..." }` → `@Valid` → insert → `201`. Blank name → `400`.
4. **Update:** `PUT /api/order-statuses/{id}` → id exists → update `name` → `200`; id missing → `404`.
5. **Delete:** `DELETE /api/order-statuses/{id}` → existed → delete, return deleted DTO → `200`; else `404`.
6. **Change an order's status (provided):** `POST /api/order/{order_id}/status` with `{ "status": "..." }`
   → `200` `{ "status": ... }`; blank/invalid status → `400`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/order-statuses` (kebab-case). Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Success | Error | Status |
|--------|----------|---------|---------|-------|--------|
| GET | `/api/order-statuses` | List all statuses | `200` + array | — | **new** |
| GET | `/api/order-statuses/{id}` | Get one status | `200` | `404` | **new** |
| POST | `/api/order-statuses` | Create status | `201` (with `id`) | `400` blank name | **new** |
| PUT | `/api/order-statuses/{id}` | Update status | `200` | `404` | **new** |
| DELETE | `/api/order-statuses/{id}` | Delete status | `200` (deleted data) | `404` | **new** |
| POST | `/api/order/{order_id}/status` | Change an order's status by name | `200` `{status}` | `400` | provided |

**Files involved:**
- DTO — [`dtos/orderStatus/ApiOrderStatusCrudDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/orderStatus/ApiOrderStatusCrudDTO.java) (plus provided `ApiOrderStatusDTO`)
- Repository — [`repository/OrderStatusRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/OrderStatusRepository.java)
- Service — [`service/OrderStatusService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/OrderStatusService.java)
- Controller — [`controller/api/OrderStatusApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/OrderStatusApiController.java)
- Test — [`api/orderStatus/OrderStatusApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/orderStatus/OrderStatusApiControllerTest.java)

---

## Data Used or Modified

**Table:** `order_statuses`. **Entity:** `OrderStatus` (frozen).

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
- **FK note:** `order_statuses` is referenced by `orders.order_status_id`. Deleting a status still
  referenced by an order violates the FK → `400` (via the `DataAccessException` handler). The delete
  test uses a **freshly created** status, so it is unreferenced and deletes cleanly.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- Native `INSERT` returns void → create is two steps: `saveOrderStatus(name)` then
  `getLastInsertedId()` in one `@Transactional` service method (same connection, correct id).
- Reuse `mapOrderStatusToDTO(...)` and the existing JPA methods.
- Controller uses `ResponseBuilder.buildOkResponse` (200) / `buildCreatedResponse` (201); throws
  `ResourceNotFoundException` (404) — no try/catch. `@Valid` covers the blank-name case.
- 404 detail message: `Order status with id {id} not found` (matches the API reference).
- The CRUD service `updateOrderStatus(int, ApiOrderStatusCrudDTO)` is distinct from the provided
  `updateOrderStatusForOrder(...)` (which changes an *order's* status).

---

## Acceptance Criteria

- [ ] `ApiOrderStatusCrudDTO` has `id` and `name`, `@NotBlank` on `name`, Lombok accessors
      compatible with `mapOrderStatusToDTO(...)`.
- [ ] All `OrderStatusRepository` CRUD native queries implemented with parameterized bindings and
      correct `order_statuses` columns; write queries carry `@Modifying` + `@Transactional`.
- [ ] The five DTO-based `OrderStatusService` methods implemented; create returns the DTO with a
      generated `id`; get-by-id/update return `Optional.empty()` when absent; delete returns a boolean.
- [ ] `OrderStatusApiController` implements the five CRUD endpoints with constructor injection,
      correct verbs, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and `ResourceNotFoundException`;
      the provided custom endpoint is unchanged.
- [ ] Responses match `Requirements/api_reference.md` (field names, envelope, 200/201/400/404).
- [ ] `OrderStatusApiControllerTest` passes:
  - [ ] Custom `POST /api/order/{order_id}/status` valid → 200 `data.status`; invalid status → 400.
  - [ ] `GET /api/order-statuses` → 200 array; `GET /{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/order-statuses` valid → 201 with matching `name`; `{}` → 400.
  - [ ] `PUT /api/order-statuses/{id}` valid+known → 200 reflecting the new name; unknown → 404.
  - [ ] `DELETE /api/order-statuses/{id}` known → 200; unknown → 404.
- [ ] Tests use `@Transactional` rollback and seeded/created ids (statuses id 1–3 and orders exist
      from seeding); delete/update-success create a fresh status first.
- [ ] No provided/frozen code modified; `./mvnw test` green once the project compiles.

---

## Notes for the AI

- Mirror the completed **CourierStatus** workflow on `dev` for the CRUD side — it is the closest
  reference (same `id`/`name` shape).
- Keep the two DTOs straight: `ApiOrderStatusDTO` (`{status}`, provided, used by the custom
  endpoint) vs `ApiOrderStatusCrudDTO` (`{id, name}`, this feature's CRUD DTO).
- Keep the diff additive: only fill the `// todo:` sections; preserve all provided reference code.
