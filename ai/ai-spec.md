# 🤖 AI_SPEC — Rocket Food Delivery REST API (Module 12)

> **READ ME FIRST.** This is the **main, project-wide AI specification**. It must be read and
> understood **before implementing any feature** and before generating any code. Every feature
> specification under [`./ai/features/`](./features/) is a companion to this file and must be used
> **together with** it. If a feature spec ever conflicts with this document, this document wins;
> if this document ever conflicts with the official **Requirement Checklist**
> (`Requirements/FSD Grading Sheets`), the checklist wins.

---

## Project Identity

- **Project Name:** Rocket Food Delivery — REST API (Module 12)
- **Short Description:** A JSON REST API that acts as the bridge between Rocket Food Delivery's
  customer-facing mobile app and its MySQL database, connecting restaurants, customers, and
  couriers in a food-delivery marketplace. It is built on top of the existing Module 11 back
  office and database.
- **Project Type:** Java / Spring Boot 3 REST API (server-side backend) with an existing
  Thymeleaf back-office layer.
- **Business Context:** Module 12 of the Full-Stack Development Program. The developer acts as a
  Junior Developer at Genesis Solutions completing the REST API for the client, Rocket Food
  Delivery, using Test-Driven Development (TDD).

---

## Goal and Scope

### Goal

Complete the Rocket Food Delivery REST API so that it is **robust, predictable, and covered by
automated tests**. The API must expose CRUD and workflow endpoints for every core entity, backed
by **native SQL queries**, **DTO-based** request/response shaping, service-layer business logic,
global exception handling, and JWT authentication. Work is done **test-first (TDD)** wherever
possible: write the test, then implement the code that makes it pass.

### In Scope (Build Now)

Complete the missing pieces of the API side for the following table workflows. For **each** entity,
implement the full vertical slice — **DTOs → Repository (native SQL) → Service (DTO-based) →
`@RestController` → Tests**:

- Address, Courier, CourierStatus, Customer, Employee, Order, OrderStatus, Product,
  ProductOrder, Restaurant, User.
- Native SQL queries (`@Query(nativeQuery = true)`) with **parameterized bindings** for all
  repository data access required by these workflows.
- DTO classes for API input/output (`ApiXxxDTO`, `ApiCreateXxxDTO`, `ApiUpdateXxxDTO`, …).
- Service-layer business logic: entity ↔ DTO mapping, validation, order creation, cascade
  deletion, status transitions.
- `@RestController` endpoints returning JSON via `ResponseBuilder`.
- Integration tests (JUnit 5 + MockMvc) covering **success and failure** paths for each endpoint.
- Supporting deliverables: a per-feature spec under `./ai/features/`, a Postman collection
  (`PostmanCollection.json` at repo root), `README.md`, and `CONCEPTS.md`.

### Out of Scope (Do NOT Build)

- **Do not modify the provided base code.** Specifically, do not change the entity models, the
  already-working `AuthApiController`, the global exception handler (`ApiExceptionHandler`),
  `ResponseBuilder`, `DataSeeder`, `SecurityConfig`, or the JWT classes. All new work is
  **additive** (complete the `// todo:` stubs; do not rewrite what already works).
- Do **not** re-implement the Module 11 back office (`@Controller` + Thymeleaf). It already works
  on JPA and is untouched by Module 12 except where an API workflow reuses its services.
- Do **not** build the mobile app or any frontend UI — the client designs that separately.
- No new frameworks, libraries, or architectural patterns beyond those already in
  [`pom.xml`](../pom.xml). No switching the ORM, DB, or auth mechanism.
- No features, endpoints, or fields not described in the API reference or a feature spec — avoid
  feature creep and over-engineering.
- Do **not** rewrite native SQL as JPA derived queries (or vice versa) where the workflow
  requires native SQL.

---

## Users and Use Cases

The API serves the mobile app on behalf of three role types (roles are derived from the
authenticated user; a user may hold more than one):

- **Customer:** places orders, views restaurants/products, views and rates their orders.
- **Courier:** is assigned to orders and updates delivery status.
- **Employee / Restaurant staff:** manages restaurant, products, and order fulfilment.

On authentication, `POST /api/auth` returns the `user_id` plus `customer_id` / `courier_id` when
the user holds those roles (`null` otherwise).

---

## Feature Index (Links Only)

Each table workflow has its own feature spec that **must be read alongside this file**:

- [`./features/address-table-workflow.feature.md`](./features/address-table-workflow.feature.md)
- [`./features/courier-table-workflow.feature.md`](./features/courier-table-workflow.feature.md)
- [`./features/courierStatus-table-workflow.feature.md`](./features/courierStatus-table-workflow.feature.md)
- [`./features/customer-table-workflow.feature.md`](./features/customer-table-workflow.feature.md)
- [`./features/employee-table-workflow.feature.md`](./features/employee-table-workflow.feature.md)
- [`./features/order-table-workflow.feature.md`](./features/order-table-workflow.feature.md)
- [`./features/orderStatus-table-workflow.feature.md`](./features/orderStatus-table-workflow.feature.md)
- [`./features/product-table-workflow.feature.md`](./features/product-table-workflow.feature.md)
- [`./features/productOrder-table-workflow.feature.md`](./features/productOrder-table-workflow.feature.md)
- [`./features/restaurant-table-workflow.feature.md`](./features/restaurant-table-workflow.feature.md)
- [`./features/user-table-workflow.feature.md`](./features/user-table-workflow.feature.md)

---

## API Routes (Project Map)

This is a backend/API project. The full request/response contract lives in
[`Requirements/api_reference.md`](../Requirements/api_reference.md) — **treat it as the source of
truth for JSON shapes, field names, and status codes.** Endpoint families:

| Area | Base Route | Notes |
|------|-----------|-------|
| Authentication | `POST /api/auth` | Public. Returns JWT + role ids. **Provided — do not modify.** |
| Users | `/api/users` | CRUD |
| Accounts | `/api/account`, `/api/auth`-related | Account read/update |
| Addresses | `/api/addresses` | CRUD |
| Restaurants | `/api/restaurants` | CRUD + average rating (reference reads provided) |
| Products | `/api/products` | CRUD |
| Customers | `/api/customers` | CRUD |
| Couriers | `/api/couriers` | CRUD |
| Courier Statuses | `/api/courier-statuses` | CRUD |
| Employees | `/api/employees` | CRUD |
| Orders | `/api/orders` | Create, read, status transitions, courier assignment, rating |
| Order Statuses | `/api/order-statuses` | CRUD |
| Product Orders | `/api/product-orders` | Join-table workflow |

- Every `/api/**` route except `POST /api/auth` requires `Authorization: Bearer <token>`.
- The Module 11 back office lives under `/backoffice/**` (form login, session-based) and is **not**
  part of Module 12 API work.
- Exact paths, path/query params, and request bodies for each endpoint are defined in the API
  reference and in the relevant feature spec — do not invent routes.

---

## Data and Models

- **Database:** MySQL (schema inherited unchanged from Module 11).
- **ORM:** Spring Data JPA / Hibernate. Entities live in
  [`models/`](../src/main/java/com/rocketFoodDelivery/rocketFood/models/) and are **frozen** for
  Module 12.
- **Core tables / entities:** `users`, `addresses`, `restaurants`, `products`, `customers`,
  `couriers`, `courier_statuses`, `employees`, `orders`, `order_statuses`, `product_orders`.
- **Key relationships (high level):** a restaurant has an address and many products; a customer
  and an employee reference a user and an address; an order links a customer, a restaurant, a
  courier, and an order status; `product_orders` is the join between orders and products
  (quantity, unit cost).
- **Seeding:** `DataSeeder` populates test data on startup under the `manual-seeding` profile.
  It is **provided — do not modify**.
- DTOs (not entities) define what crosses the API boundary — see Coding Standards below.

---

## Tech Stack and Tools

Use **only** the technologies already declared in [`pom.xml`](../pom.xml). Do not add dependencies.

### Backend
- **Java 21** (as configured in `pom.xml`; the business brief's "Java 17" is superseded by the
  actual project configuration).
- **Spring Boot 3.4** — `spring-boot-starter-web`, `-data-jpa`, `-validation`, `-security`,
  `-thymeleaf` (back office only).
- **Hibernate** (via Spring Data JPA).
- **Lombok** for boilerplate (`@Data`, `@Getter/@Setter`, `@AllArgsConstructor`,
  `@NoArgsConstructor`, `@Builder`).
- **JWT:** `io.jsonwebtoken:jjwt` (stateless API auth) — provided.

### Database
- **MySQL** (`mysql-connector-j`). Data access for API workflows uses **native SQL** via
  `@Query(nativeQuery = true)`.

### Testing
- **JUnit 5** + **Spring Boot Test** + **MockMvc** (integration tests against the full context).
- Tests use `@SpringBootTest` and `@AutoConfigureMockMvc(addFilters = false)` to bypass JWT.

### Build / Tooling
- **Maven** (`./mvnw`). **Postman** for manual endpoint verification
  (`PostmanCollection.json` at repo root). `javafaker` is available for seed/test data.

---

## Repository Structure

```
Module12/
├── ai/
│   ├── ai-spec.md                 ← THIS FILE (read first)
│   └── features/                  ← one *.feature.md per table workflow
├── src/main/java/com/rocketFoodDelivery/rocketFood/
│   ├── controller/
│   │   ├── api/                   ← @RestController (JSON) — Module 12 work goes here
│   │   └── backoffice/            ← @Controller + Thymeleaf (Module 11, frozen)
│   ├── dtos/                      ← DTOs grouped by entity subfolder (address/, order/, …)
│   ├── models/                    ← JPA entities (frozen)
│   ├── repository/                ← JPA + native SQL queries
│   ├── service/                   ← business logic + entity↔DTO mapping
│   ├── exception/                 ← custom exceptions + ApiExceptionHandler (frozen)
│   ├── security/                  ← JWT + SecurityConfig (frozen)
│   ├── util/                      ← ResponseBuilder (frozen)
│   └── DataSeeder.java            ← seed data (frozen)
├── src/main/resources/
│   └── application.properties     ← DB + JWT config (fill in local DB credentials)
├── src/test/java/.../api/         ← MockMvc integration tests, one subfolder per entity
├── Requirements/                  ← source docs (git-ignored; reference only)
├── PostmanCollection.json         ← all endpoints, ready to run (deliverable)
├── README.md · CONCEPTS.md
└── pom.xml
```

**File placement rules:** put new DTOs in `dtos/<entity>/`, native queries in the matching
`repository/*Repository.java`, business logic in `service/*Service.java`, endpoints in
`controller/api/*ApiController.java`, and tests in `src/test/java/.../api/<entity>/`. Do not
create files outside these locations.

---

## Coding Standards / Conventions

### Architecture (dual, layered)
- **API path (Module 12):** `Client → @RestController → Service → Repository (native SQL) → DB`,
  with **DTOs** crossing the controller/service boundary.
- **Back office (Module 11, frozen):** `Browser → @Controller → Service → Repository (JPA) →
  Thymeleaf`.
- **Layer discipline:** controllers only parse the request, delegate to a service, and return a
  response. **All business logic lives in the service layer.** Repositories only do data access.

### Naming (must match exactly)
| Location | Convention | Example |
|----------|-----------|---------|
| DB table / column | snake_case | `order_statuses`, `price_range` |
| Java class | PascalCase | `OrderStatus` |
| Java variable | camelCase | `priceRange` |
| DTO class | `ApiXxxDTO` | `ApiRestaurantDTO` |
| JSON field | snake_case | `"price_range": 2` |
| API endpoint | kebab-case | `/api/order-statuses` |
| Test class | `XxxApiControllerTest` | `RestaurantApiControllerTest` |

- Map camelCase Java fields to snake_case JSON with `@JsonProperty("snake_case")`.

### DTOs
- Use Lombok: `@Getter @Setter @AllArgsConstructor @NoArgsConstructor` (add `@Builder` where
  helpful). Include **only** fields needed for API I/O — never expose passwords or internal-only
  columns. Put request-validation annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Max`) on
  request DTOs and validate with `@Valid`.

### Repositories (native SQL)
- Repositories still `extends JpaRepository<Entity, Id>` — built-in methods stay available; add
  native SQL only where built-ins are not enough.
- Use `@Query(nativeQuery = true)` with **`:named` parameters bound via `@Param`** (positional
  `?1` is allowed but named is preferred for readability) — **never** concatenate strings into SQL
  (injection risk; automatically non-compliant).
- Add `@Modifying` + `@Transactional` for `INSERT`/`UPDATE`/`DELETE`; these return `int` (rows
  affected) or `void` — match the return type already used by neighbouring methods in the same
  repository.
- For computed/multi-column result rows, return `List<Object[]>` and map to a DTO in the service
  (see `mapRowToRestaurantDTO` in `RestaurantService` as the reference pattern).

### Controllers
- Annotate with `@RestController`; inject services via constructor injection.
- Use `@GetMapping/@PostMapping/@PutMapping/@PatchMapping/@DeleteMapping`, `@RequestBody`
  (+ `@Valid`), `@PathVariable`, `@RequestParam` as appropriate. Use `@PatchMapping` for
  **partial** updates (e.g., an order status transition); `@PutMapping` for full-resource updates.
- Return success via `ResponseBuilder.buildOkResponse(...)` / `buildCreatedResponse(...)`.
- **Do not write try/catch** for expected errors — `throw` `ResourceNotFoundException` (404),
  `BadRequestException` (400), or `ValidationException` (400) and let `ApiExceptionHandler`
  produce the JSON error response.

### Response contract
- Success: `{ "message": "Success", "data": { … } }` (or array).
- Error: `{ "error": "...", "details": "..." }` with the correct HTTP status.
- Correct status codes: `200 OK`, `201 Created`, `400 Bad Request`, `404 Not Found`,
  `401 Unauthorized`.

### Commit Messages (Conventional Commits)
- Format: `<type>(optional scope): <short summary>` — e.g. `feat(order): add courier assignment
  endpoint`, `test(address): cover create/update failure paths`.
- Common types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`. Extended/custom
  types when they fit: `init`, `perf`, `build`, `ci`, `revert`, `typo`, `wip`, `deps`.
- Summary is short, imperative, and states **what** changed; the scope names the affected
  area/entity. See `Requirements/Commit Message Convention.docx` and
  [conventionalcommits.org](https://www.conventionalcommits.org/).

### Tests
- `@SpringBootTest` + `@AutoConfigureMockMvc` (use `addFilters = false` to bypass JWT, as the
  reference tests do); add `@Transactional` on the class so DB changes roll back after each test.
  Inject `MockMvc` and `ObjectMapper`.
- Tests run against **`DataSeeder`** data. Use the known seeded IDs/values in assertions; **never
  hard-code IDs that don't match the seeded records.**
- Cover **both success and failure** for every endpoint (valid input/expected data **and** missing
  record / bad input / wrong status). Follow the Red → Green → Refactor TDD cycle of the provided
  reference tests (write failing test → minimal code to pass → refactor with tests green).

---

## How to Run / Test the Project

1. **Configure the database** in [`application.properties`](../src/main/resources/application.properties):
   set `spring.datasource.url` (database name), `username`, and `password`. Ensure MySQL is
   running and the Module 11 schema exists.
2. **Seeding:** the `manual-seeding` profile is active by default and seeds data on startup.
3. **Run the API:** `./mvnw spring-boot:run` (serves on `http://localhost:8080`).
4. **Run the tests:** `./mvnw test`.
5. **Authenticate:** `POST /api/auth` with a seeded email/password to get a JWT, then send it as
   `Authorization: Bearer <token>` on all other `/api/**` calls.
6. **Verify endpoints** with the `PostmanCollection.json` collection (no per-request edits needed).

**Environment / secrets:** DB credentials in `application.properties`; JWT secret is
`app.jwt.secret`. The submission-summary document holds any credentials and **must not be
committed to GitHub**.

---

## Rules for the AI

Strict rules to follow when generating code for this project:

1. **Read this spec and the relevant feature spec first.** Never implement a workflow without both.
2. **Do not modify frozen/provided code:** entity models, `AuthApiController`, `ApiExceptionHandler`,
   `ResponseBuilder`, `DataSeeder`, `SecurityConfig`, JWT classes, and the Module 11 back office.
   All work is additive — complete the `// todo:` stubs.
3. **Follow the implementation order:** DTOs → Repository (native SQL) → Service → Controller →
   Tests. Prefer **test-first (TDD)**.
4. **Native SQL only where required**, always with parameterized `:named` bindings — never string
   concatenation.
5. **Keep layers clean:** logic in services, thin controllers, data access in repositories.
6. **Match the API reference contract exactly** — routes, JSON field names (snake_case),
   status codes. Do not invent endpoints or fields.
7. **Reuse existing patterns and helpers** (`ResponseBuilder`, custom exceptions,
   `mapRowTo…DTO`) instead of introducing new ones.
8. **No new dependencies, frameworks, or advanced patterns.** Keep the code junior-friendly and
   consistent with existing files (imports style, Lombok usage, formatting).
9. **Do not add features that are not requested.** When a requirement is ambiguous, prefer the
   API reference and feature spec; surface the ambiguity rather than guessing.
10. **Explain changes briefly** and keep diffs minimal and scoped to the feature at hand.

---

## Global Definition of Done

A feature/table workflow is **done** only when all of the following are true. These apply
**across every feature** and complement each feature spec's own acceptance criteria.

### Per-feature (repeat for every table workflow)
- [ ] Required DTOs implemented with correct fields, Lombok, and `@JsonProperty` mapping.
- [ ] Repository methods implemented with native SQL + parameterized bindings (and
      `@Modifying`/`@Transactional` where writing).
- [ ] Service methods implemented; entity↔DTO mapping and business logic in the service layer.
- [ ] `@RestController` endpoints implemented, returning the standard response contract and
      correct status codes.
- [ ] Integration tests (MockMvc) cover **success and failure** paths and pass.
- [ ] A matching feature spec exists in `./ai/features/` and was followed.

### Cross-feature / project-wide
- [ ] `./mvnw test` passes — the whole suite is green.
- [ ] Project builds and runs without errors (`./mvnw spring-boot:run`).
- [ ] No provided/frozen code was modified; all changes are additive.
- [ ] All SQL uses parameterized bindings (no string concatenation anywhere).
- [ ] JSON shapes, field names, and status codes match `Requirements/api_reference.md`.
- [ ] `PostmanCollection.json` at the repo root covers all endpoints and runs without edits.
- [ ] `README.md` (Title, Description, Tech Stack, Structure, Setup, Env Vars, API Docs, Author)
      and `CONCEPTS.md` (3 concepts) are complete and accurate.
- [ ] Naming conventions and file placement respected; minimal spelling/formatting errors.
- [ ] Git workflow honored: `feature/*` → `dev` → `main`, **no direct commits to `main`**; only
      `main` is graded, so it must reflect the final stable version.
- [ ] Commit messages follow the Conventional Commits format (`<type>(scope): summary`).
- [ ] Submission-summary document prepared **and kept out of Git**.
