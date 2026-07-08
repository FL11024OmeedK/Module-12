# 🤖 AI_FEATURE — Restaurant Table Workflow

> **Must be read and used together with the Global AI Spec** ([`../ai-spec.md`](../ai-spec.md)).
> The global spec's rules on architecture, naming, native SQL, DTOs, the response envelope,
> exception handling, and the Definition of Done all apply here. This document only adds what is
> **specific to the Restaurant entity**. If anything here conflicts with the global spec or
> [`Requirements/api_reference.md`](../../Requirements/api_reference.md), those win.

---

## Feature Identity

- **Feature Name:** Restaurant Table Workflow
- **Related Area:** Backend (REST API) — DTO → Repository (native SQL) → Service → `@RestController` → Tests
- **Branch:** `feature/restaurant-table-workflow` (created from `dev`, merged back via PR)

---

## Feature Goal

Expose CRUD over the `restaurants` table through the JSON REST API. A restaurant links a **user** and
an **address** (created together with the restaurant), and has `name`, `phone`, `email`, and a
`price_range` (1–3). Read endpoints also expose a **computed average `rating`** (from orders'
`restaurant_rating`). This is the last workflow that unblocks the full project build (its service
maps to the currently empty `ApiRestaurantDTO`).

This workflow is **partially provided**. Already implemented (do NOT modify): the two native rating
queries (`findRestaurantWithAverageRatingById`, `findRestaurantsByRatingAndPriceRange`),
`getLastInsertedId`, and the `mapRowToRestaurantDTO` helper (maps an `Object[]` row →
`{id, name, price_range, rating}`).

---

## Feature Scope

### In Scope (Included)

Complete the existing `// todo:` stubs:

- **DTOs** — `ApiRestaurantDTO` (response) and `ApiCreateRestaurantDTO` (create/update request, with a
  nested `ApiAddressDTO`).
- **Repository** — native SQL `saveRestaurant`, `findAllRestaurants`, `findRestaurantById`,
  `updateRestaurant`, `deleteRestaurantById`. (Rating queries + `getLastInsertedId` are done.)
- **Service** — DTO-based create, read-all (with optional rating/price filter), read-by-id (with
  rating), update, delete, reusing `mapRowToRestaurantDTO`.
- **Controller** — the five REST endpoints on `RestaurantApiController`.
- **Tests** — complete the `// todo:` MockMvc tests (success + failure).

### Out of Scope (Excluded)

- Do **not** modify the `Restaurant`, `User`, or `Address` entities (frozen).
- Do **not** modify the provided rating queries, `getLastInsertedId`, or `mapRowToRestaurantDTO`.
- Do **not** implement User CRUD — only reference `user_id`. Reuse the existing Address persistence
  (the restaurant's address is created as part of restaurant creation).
- No auth/role logic beyond the global JWT rule (tests bypass it with `addFilters = false`).
- No `active` field in the API (managed internally, defaults `true` on create).

---

## Sub-Requirements (Feature Breakdown)

Implement in the global spec's order: **DTO → Repository → Service → Controller → Tests.**

1. **`ApiRestaurantDTO`** (response) — one DTO serving two shapes via `@JsonInclude(NON_NULL)`:
   - `int id`, `String name`, `int priceRange` → `@JsonProperty("price_range")` (always present).
   - `Integer rating` — set by `mapRowToRestaurantDTO` (GET/DELETE); omitted on create/update.
   - `String phone`, `String email`, `Integer userId` → `@JsonProperty("user_id")`,
     `ApiAddressDTO address` — set on create/update; omitted on GET/DELETE.
   - Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`. Setters must match
     `mapRowToRestaurantDTO` (`setId(int)`, `setName(String)`, `setPriceRange(int)`, `setRating(int)`).

2. **`ApiCreateRestaurantDTO`** (create + update request):
   - `int userId` → `@JsonProperty("user_id")`, `String name` `@NotBlank`, `String phone` `@NotBlank`,
     `String email` `@Email`, `int priceRange` → `@JsonProperty("price_range")` `@Min(1) @Max(3)`,
     `ApiAddressDTO address` (`@Valid`, **not** `@NotNull` — see below).
   - Lombok `@Getter @Setter @AllArgsConstructor @NoArgsConstructor`.

3. **`RestaurantRepository` (native SQL)** — fill the `@Query` bodies (positional where no `@Param`,
   named where present):
   - `saveRestaurant(userId, addressId, name, priceRange, phone, email)` — `INSERT`; set `active = true`
     and `created_on`/`update_on` via `NOW()`.
   - `findAllRestaurants()` → `List<Restaurant>`.
   - `findRestaurantById(:restaurantId)` → `Optional<Restaurant>`.
   - `updateRestaurant(restaurantId, name, priceRange, phone)` — `UPDATE` those three columns only
     (+ `update_on = NOW()`).
   - `deleteRestaurantById(:restaurantId)` — `DELETE`.

4. **`RestaurantService` (DTO methods)** — implement the `// todo:` methods:
   - **Create from DTO:** validate `user_id` exists (→ `400`); validate `address` is present (→ `400`);
     persist the address (JPA), then `saveRestaurant(...)` (native) + `getLastInsertedId()`; return the
     **detailed** DTO (`id`, `name`, `price_range`, `phone`, `email`, `user_id`, `address` with its id).
   - **Read all:** `findRestaurantsByRatingAndPriceRange(rating, priceRange)` → rows →
     `mapRowToRestaurantDTO` (summary shape).
   - **Read by id (with rating):** `findRestaurantWithAverageRatingById(id)` → empty → `Optional.empty()`
     (→ 404); else `mapRowToRestaurantDTO` (summary shape).
   - **Update from DTO:** existence check → `Optional.empty()` if unknown (→ 404); `updateRestaurant(...)`
     changes only `name`/`price_range`/`phone`; return the **detailed** DTO (updated fields + stored
     `email`/`user_id`/`address`).
   - **Delete by id:** `boolean`.
   - Reuse `mapRowToRestaurantDTO(...)`; keep all logic in the service. Inject `UserRepository` for the
     create-time user check.

5. **`RestaurantApiController`** — declare `private final RestaurantService`, constructor-inject it, and
   implement the five endpoints (see Interfaces). `GET /api/restaurants` takes optional `rating` and
   `price_range` query params. Thin controller: delegate, wrap success with `ResponseBuilder`, throw
   exceptions.

6. **`RestaurantApiControllerTest`** — complete the stubbed tests following TDD (success + failure).

---

## User Flow / Logic (High Level)

1. **List:** `GET /api/restaurants` (optional `?rating=&price_range=`) → summary DTOs with rating → `200`.
2. **Read one:** `GET /api/restaurants/{id}` → summary DTO with rating → `200`; not found → `404`.
3. **Create:** `POST /api/restaurants` (user, details, nested address) → `@Valid` → user exists & address
   present → create address + restaurant (`active = true`) → `201` with the detailed DTO.
4. **Update:** `PUT /api/restaurants/{id}` (`name`, `price_range`, `phone`) → id exists → update → `200`;
   id missing → `404`.
5. **Delete:** `DELETE /api/restaurants/{id}` → existed → delete, return summary DTO → `200`; else `404`.

---

## Interfaces (Pages, Endpoints, Screens)

### Frontend
None. API-only feature.

### Backend / API

Base path `/api/restaurants`. Success envelope: `{ "message": "Success", "data": ... }`.

| Method | Endpoint | Purpose | Response shape | Success | Error |
|--------|----------|---------|----------------|---------|-------|
| GET | `/api/restaurants` (`?rating=&price_range=`) | List (filterable) | summary + rating | `200` array | — |
| GET | `/api/restaurants/{id}` | Get one with rating | summary + rating | `200` | `404` |
| POST | `/api/restaurants` | Create (+ address) | detailed | `201` | `400` invalid/missing |
| PUT | `/api/restaurants/{id}` | Update name/price/phone | detailed | `200` | `404` · `400` |
| DELETE | `/api/restaurants/{id}` | Delete | summary + rating | `200` | `404` |

**Summary shape:** `{id, name, price_range, rating}`. **Detailed shape:** `{id, name, phone, email,
user_id, price_range, address:{id, street_address, city, postal_code}}`.

**Files involved:**
- DTOs — [`dtos/restaurant/ApiRestaurantDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/restaurant/ApiRestaurantDTO.java), [`dtos/restaurant/ApiCreateRestaurantDTO.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/dtos/restaurant/ApiCreateRestaurantDTO.java)
- Repository — [`repository/RestaurantRepository.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/repository/RestaurantRepository.java)
- Service — [`service/RestaurantService.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/service/RestaurantService.java)
- Controller — [`controller/api/RestaurantApiController.java`](../../src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/RestaurantApiController.java)
- Test — [`api/restaurant/RestaurantApiControllerTest.java`](../../src/test/java/com/rocketFoodDelivery/rocketFood/api/restaurant/RestaurantApiControllerTest.java)

---

## Data Used or Modified

**Table:** `restaurants`. **Entity:** `Restaurant` (frozen). Also **creates** an `addresses` row on create.

| Entity field (Java) | DB column | JSON key | Type | Notes |
|---------------------|-----------|----------|------|-------|
| `id` | `id` | `id` | `int` | PK, auto-increment |
| `user` (→ id) | `user_id` | `user_id` | `int` | FK → `users`, NOT NULL |
| `address` (→ id) | `address_id` | `address` (nested) | `int` | FK → `addresses`, **unique**, NOT NULL |
| `name` | `name` | `name` | `String` | NOT NULL, `@NotBlank` |
| `phone` | `phone` | `phone` | `String` | NOT NULL, `@NotBlank` |
| `email` | `email` | `email` | `String` | nullable, `@Email` |
| `priceRange` | `price_range` | `price_range` | `int` | NOT NULL, 1–3, default 1 |
| `active` | `active` | — | `boolean` | NOT NULL, default `true`; not in API |
| — (computed) | — | `rating` | `Integer` | `CEIL(avg(orders.restaurant_rating))`, 0 if none |
| `createdOn`/`updateOn` | `created_on`/`update_on` | — | timestamps | internal |

**Validations & expected behavior:**
- **Format validation** (DTO / `@Valid`): `name` and `phone` required; `price_range` 1–3; `email`
  valid when present; nested `address` fields (`street_address`, `city`, `postal_code`) required when
  an address is present.
- **`address` is required on create but absent on update** — it is therefore validated in the
  **service** (create throws `400` when `address == null`), not via `@NotNull`, so the update body
  (`name`/`price_range`/`phone` only) passes `@Valid`.
- **Referential validation** (service): `user_id` must reference an existing user → `400`.
- **Create** persists a new address then the restaurant with `active = true`; the response echoes the
  new address id.
- **Update** changes only `name`, `price_range`, `phone`; `user_id`, `email`, and `address` are
  immutable and echoed from storage.
- **Native SQL bypasses `@CreationTimestamp`/`@UpdateTimestamp`** → `created_on`/`update_on` via `NOW()`.
- **FK note:** a restaurant referenced by products/orders cannot be deleted (FK) → `400`; the delete
  test creates a fresh restaurant (no products/orders) so it deletes cleanly.
- All native queries use parameterized bindings — no string concatenation.

---

## Tech Constraints (Feature-Level)

- One `ApiRestaurantDTO` with `@JsonInclude(JsonInclude.Include.NON_NULL)` serves both the summary
  (GET/DELETE) and detailed (POST/PUT) shapes — each service path sets only its relevant fields.
- Create: persist the address via JPA (`addressRepository.save`) to get its id, then native
  `saveRestaurant(...)` + `getLastInsertedId()` in one `@Transactional` method.
- Reuse the provided `mapRowToRestaurantDTO(...)` and the native rating queries for reads.
- Controller: `ResponseBuilder` for success; throw `ResourceNotFoundException` (404) /
  `BadRequestException` (400) — no try/catch. 404 detail: `Restaurant with id {id} not found`.

---

## Acceptance Criteria

- [ ] `ApiRestaurantDTO` (`id, name, price_range, rating, phone, email, user_id, address`) uses
      `@JsonInclude(NON_NULL)`, has `@JsonProperty` mapping, and Lombok setters compatible with
      `mapRowToRestaurantDTO(...)`.
- [ ] `ApiCreateRestaurantDTO` (`user_id, name @NotBlank, phone @NotBlank, email @Email,
      price_range @Min(1) @Max(3), address @Valid`) is correct; `address` is validated in the service
      on create (not `@NotNull`).
- [ ] All `RestaurantRepository` CRUD native queries implemented with parameterized bindings and
      correct `restaurants` columns; create sets `active = true`; update touches only name/price/phone;
      write queries carry `@Modifying` + `@Transactional`.
- [ ] `RestaurantService` DTO methods implemented; create validates user + address (400), persists
      address then restaurant, returns the detailed DTO; read-by-id/all use the rating queries; update
      returns `Optional.empty()` for an unknown id; delete returns a boolean.
- [ ] `RestaurantApiController` implements all five endpoints with constructor injection, the optional
      `rating`/`price_range` query params, `@Valid @RequestBody`, `ResponseBuilder` envelopes, and
      `ResourceNotFoundException`.
- [ ] Responses match `Requirements/api_reference.md` (summary vs detailed shapes, envelope, 200/201/400/404).
- [ ] `RestaurantApiControllerTest` passes with success + failure cases:
  - [ ] `GET /api/restaurants` → 200, array; `GET /{id}` → 200 with `data.id`; unknown → 404.
  - [ ] `POST /api/restaurants` valid (with nested address, existing user) → 201 with matching fields
        incl. nested address; missing address → 400.
  - [ ] `PUT /api/restaurants/{id}` valid+known → 200 reflecting name/price/phone; unknown → 404.
  - [ ] `DELETE /api/restaurants/{id}` (fresh restaurant) → 200; unknown → 404.
- [ ] Tests build a valid `user_id` from seeded data, use `@Transactional` rollback, and don't
      hard-code non-seeded ids.
- [ ] No provided/frozen code modified; **`./mvnw test` now compiles and the whole suite runs** (this
      is the last build blocker).

---

## Notes for the AI

- **This is the last compile blocker** — after it lands, the entire project compiles and every
  workflow's tests run. Verify with a full `./mvnw test` at the end (not just this suite).
- **Two response shapes from one DTO:** don't invent a second DTO class — use `@JsonInclude(NON_NULL)`
  and populate summary fields (via `mapRowToRestaurantDTO`) or detailed fields per endpoint.
- Create must persist the **address first** (to get its id) then the restaurant. Reuse `ApiAddressDTO`.
- Delete/update tests: create a **fresh** restaurant (with a fresh address, existing user) so it has no
  products/orders and can be deleted; update can target a seeded restaurant under `@Transactional`.
- Keep the diff additive: only fill `// todo:` sections; preserve all provided reference code.
