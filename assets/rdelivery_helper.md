# Rocket Delivery Helper — Module 12

## Overview

This module builds on Module 11 by transitioning from **MVC with Thymeleaf views** to **RESTful APIs with JSON responses**. You will implement DTOs, Native SQL queries, `@RestController` endpoints, global exception handling, JWT authentication, and automated tests using MockMvc.

---

## What Changed from Module 11?

| Concept | Module 11 | Module 12 |
|---------|-----------|-----------|
| **Controller** | `@Controller` → returns HTML views | `@RestController` → returns JSON data |
| **Repository** | JPA auto-generated (`save()`, `findAll()`) | Native SQL queries with `@Query(nativeQuery = true)` |
| **Data Transfer** | Entity objects passed directly to views | DTOs (Data Transfer Objects) shape the API responses |
| **Service** | Works with entities directly | Maps entities ↔ DTOs, handles business logic |
| **Views** | Thymeleaf HTML templates | No views — JSON responses only (for API) |
| **Authentication** | Form login (session-based) | JWT token-based (stateless) for APIs |
| **Error Handling** | Thymeleaf error pages | `@RestControllerAdvice` with JSON error responses |
| **Testing** | Manual testing via browser/DataSeeder | Automated tests with MockMvc + JUnit |

---

## Architecture Pattern

This module follows a **dual architecture**:

```
API Path:    Client (Postman) → @RestController → Service → Repository (Native SQL) → Database
                                      ↕ DTOs ↕

Backoffice:  Browser → @Controller → Service → Repository (JPA) → Database
                          ↕ Thymeleaf Views ↕
```

**Implementation Order:**
```
1. DTOs → 2. Repository (Native SQL) → 3. Service (DTO methods) → 4. @RestController → 5. Tests
```

> **Note:** The backoffice (`@Controller`) side from Module 11 already works with JPA. Module 12 adds the API side (`@RestController`) that uses DTOs and Native SQL.

---

## Key Concepts

### What is a DTO? (Data Transfer Object)

A DTO is a simple Java class that defines the **shape of data** sent to or received from the API. It separates your database structure (entity) from what the client sees (API response).

**Why use DTOs?**
- The client doesn't need to see every database column (e.g., passwords, internal IDs)
- API responses can combine data from multiple tables (e.g., restaurant + average rating)
- You control the exact JSON field names using `@JsonProperty`

**Entity (what the database stores):**
```java
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    private Integer id;
    private String name;
    @Column(name = "price_range")
    private int priceRange;     // camelCase in Java
    // ... many more fields
}
```

**DTO (what the API returns):**
```java
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ApiRestaurantDTO {
    private int id;
    private String name;
    @JsonProperty("price_range")    // JSON key will be snake_case
    private int priceRange;
    private int rating;             // computed field, not stored in DB
}
```

> **Key:** Use `@JsonProperty("snake_case")` when the JSON field name differs from the Java field name.

---

### What is a Native SQL Query?

In Module 11, Spring Data JPA automatically generated SQL queries from method names. In Module 12, you write the SQL yourself using `@Query(nativeQuery = true)`.

**Module 11 (JPA auto-generated):**
```java
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
    // JPA automatically creates: SELECT * FROM restaurants WHERE id = ?
    Optional<Restaurant> findById(int id);
}
```

**Module 12 (Native SQL — you write it):**
```java
@Query(nativeQuery = true, value = """
    SELECT * FROM restaurants WHERE id = :restaurantId
""")
Optional<Restaurant> findRestaurantById(@Param("restaurantId") int restaurantId);
```

**When to use Native SQL:**
- Complex queries with JOINs and aggregations (e.g., average rating)
- When you need full control over the SQL
- For INSERT, UPDATE, DELETE operations with `@Modifying` + `@Transactional`

**Native SQL patterns:**

| Operation | Annotations Needed | Return Type |
|-----------|-------------------|-------------|
| SELECT (single) | `@Query` | `Optional<Entity>` |
| SELECT (list) | `@Query` | `List<Entity>` or `List<Object[]>` |
| SELECT (computed) | `@Query` | `List<Object[]>` (raw rows) |
| INSERT | `@Modifying` + `@Transactional` + `@Query` | `void` |
| UPDATE | `@Modifying` + `@Transactional` + `@Query` | `void` |
| DELETE | `@Modifying` + `@Transactional` + `@Query` | `void` |

**Important:** When your SQL returns computed columns (like `COALESCE(CEIL(...)) AS rating`), you must use `List<Object[]>` and manually map each column to a DTO in the service layer.

---

### @RestController vs @Controller

| Feature | `@Controller` (Module 11) | `@RestController` (Module 12) |
|---------|--------------------------|-------------------------------|
| Returns | View name (String) → Thymeleaf renders HTML | Java object → Jackson serializes to JSON |
| Annotation | `@Controller` | `@RestController` |
| Response | `return "restaurant/restaurantList"` | `return ResponseEntity.ok(dto)` |
| Data binding | `Model model` + `model.addAttribute(...)` | `@RequestBody` + `@ResponseBody` (auto) |
| Validation | `BindingResult` + Thymeleaf errors | `@Valid` + Exception handler returns JSON |

**Example comparison:**

```java
// Module 11 — @Controller
@Controller
@RequestMapping("/backoffice/restaurants")
public class RestaurantController {
    @GetMapping
    public String listAll(Model model) {
        model.addAttribute("restaurants", service.findAll());
        return "restaurant/restaurantList";  // returns HTML view name
    }
}

// Module 12 — @RestController
@RestController
public class RestaurantApiController {
    @GetMapping("/api/restaurants")
    public ResponseEntity<Object> getAll() {
        List<ApiRestaurantDTO> dtos = service.getAllRestaurantsAsDTO();
        return ResponseBuilder.buildOkResponse(dtos);  // returns JSON
    }
}
```

---

### ResponseEntity and ResponseBuilder

`ResponseEntity<>` lets you control the HTTP status code and response body.

```java
// Direct usage
return ResponseEntity.ok(data);                              // 200 OK
return ResponseEntity.status(HttpStatus.CREATED).body(data); // 201 Created
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);// 404 Not Found

// Using ResponseBuilder utility (provided in the project)
return ResponseBuilder.buildOkResponse(data);      // { "message": "Success", "data": {...} }
return ResponseBuilder.buildCreatedResponse(data); // { "message": "Success", "data": {...} }
```

The `ResponseBuilder` wraps your data in a standard format:
```json
{
    "message": "Success",
    "data": { ... your DTO ... }
}
```

---

### Global Exception Handling

Instead of try-catch in every controller, Module 12 uses `@RestControllerAdvice` to catch exceptions globally and return consistent JSON error responses.

**How it works:**
1. Controller throws a custom exception (e.g., `throw new ResourceNotFoundException("...")`)
2. `ApiExceptionHandler` catches it automatically
3. Returns a JSON error response with the correct HTTP status

**Custom exceptions provided:**
- `ResourceNotFoundException` → returns **404 Not Found**
- `BadRequestException` → returns **400 Bad Request**
- `ValidationException` → returns **400 Bad Request** (with validation details)

**Error response format:**
```json
{
    "error": "Not Found",
    "details": "Restaurant with id 999 not found"
}
```

**Usage in controllers:**
```java
@GetMapping("/api/restaurants/{id}")
public ResponseEntity<Object> getById(@PathVariable int id) {
    ApiRestaurantDTO dto = service.getRestaurantById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            String.format("Restaurant with id %d not found", id)
        ));
    return ResponseBuilder.buildOkResponse(dto);
}
```

> **Key:** You never need to write try-catch blocks in your API controllers. Just throw the appropriate exception and the handler does the rest.

---

### JWT Authentication

Module 12 secures the API endpoints with JWT (JSON Web Tokens).

**Flow:**
1. Client sends `POST /api/auth` with email + password
2. Server validates credentials and returns a JWT token
3. Client includes the token in subsequent requests: `Authorization: Bearer <token>`
4. `JwtTokenFilter` validates the token on every API request

**Security configuration (already provided):**
- `/api/auth` → public (no token needed)
- `/api/**` → requires valid JWT token
- `/backoffice/**` → uses form login (session-based, separate from API)

> **Note:** For testing, `@AutoConfigureMockMvc(addFilters = false)` disables the JWT filter so tests don't need tokens.

---

## Naming Conventions

| Location | Convention | Example |
|----------|-----------|---------|
| Database Table | snake_case | `order_statuses`, `restaurants` |
| Database Column | snake_case | `price_range`, `user_id` |
| Java Class | PascalCase | `OrderStatus`, `Restaurant` |
| Java Variable | camelCase | `priceRange`, `userId` |
| Java File Name | PascalCase.java | `OrderStatus.java` |
| DTO Class | ApiXxxDTO.java | `ApiRestaurantDTO.java` |
| JSON Response | snake_case | `"price_range": 2`, `"user_id": 3` |
| API Endpoint | kebab-case | `/api/order-statuses`, `/api/restaurants` |
| Test Class | XxxApiControllerTest.java | `RestaurantApiControllerTest.java` |

> **Tip:** Use `@JsonProperty("snake_case_name")` on DTO fields when Java uses camelCase but the API response needs snake_case.

---

## DTO Organization

DTOs are organized by entity in subfolders:

```
dtos/
├── address/          → ApiAddressDTO
├── auth/             → AuthRequestDTO, AuthResponseSuccessDTO, AuthResponseErrorDTO
├── courier/          → ApiCourierDTO
├── courierStatus/    → ApiCourierStatusDTO
├── customer/         → ApiCustomerDTO
├── employee/         → ApiEmployeeDTO
├── order/            → ApiOrderDTO, ApiCreateOrderDTO, ApiUpdateOrderDTO, ...
├── orderStatus/      → ApiOrderStatusDTO, ApiOrderStatusCrudDTO
├── product/          → ApiProductDTO, ApiCreateProductDTO, ApiProductForOrderApiDTO
├── productOrder/     → ApiProductOrderDTO
├── response/         → ApiResponseDTO, ApiErrorDTO, ApiResponseStatusDTO
├── restaurant/       → ApiRestaurantDTO, ApiCreateRestaurantDTO, ApiRestaurantRatingDTO
└── user/             → ApiUserDTO, ApiCreateUserDTO, ApiAccountDTO, ...
```

**Common DTO patterns:**
- `ApiXxxDTO` → Response DTO (what the API returns)
- `ApiCreateXxxDTO` → Request DTO for create operations (what the client sends)
- `ApiUpdateXxxDTO` → Request DTO for update operations

---

## Development Workflow

### Step 1: Complete DTOs

Define the shape of your API request/response data.

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/dtos/`

**Tasks:**
- Add Lombok annotations (`@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`)
- Add fields matching the expected API response
- Use `@JsonProperty("snake_case")` for JSON field name mapping
- Use validation annotations (`@NotNull`, `@NotBlank`, `@Min`, `@Max`) on request DTOs

---

### Step 2: Complete Repository (Native SQL Queries)

Write the actual SQL for each CRUD operation.

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/repository/`

**Tasks:**
- Replace `// todo:` comments with actual SQL statements
- Use `:paramName` for query parameters
- Add `@Modifying` + `@Transactional` for INSERT/UPDATE/DELETE

> **Important:** The repository already has complex queries implemented (e.g., `findRestaurantWithAverageRatingById` with JOINs and aggregation). Study those as reference for simpler CRUD queries.

---

### Step 3: Complete Service (DTO Methods)

Add service methods that convert between entities and DTOs.

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/service/`

**Tasks:**
- Implement DTO-based CRUD methods (the `// todo:` sections)
- Use existing JPA methods or the new native SQL repository methods
- Map entities to DTOs using helper methods
- Return `Optional<DTO>` for single-item lookups

> **Tip:** The `mapRowToRestaurantDTO(Object[] row)` helper is already provided in `RestaurantService`. Study it to understand how `Object[]` results from native SQL are mapped to DTO fields.

---

### Step 4: Complete @RestController Endpoints

Build API endpoints that connect services to JSON responses.

**Location:** `src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/`

**Tasks:**
- Inject the service using constructor injection
- Implement CRUD endpoints following the API spec
- Use `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- Use `@RequestBody` for JSON input, `@PathVariable` for URL params, `@RequestParam` for query params
- Use `ResponseBuilder` for success responses
- Throw custom exceptions for error cases

---

### Step 5: Write Tests

Write automated integration tests using MockMvc.

**Location:** `src/test/java/com/rocketFoodDelivery/rocketFood/api/`

**Tasks:**
- Complete the `// todo:` test methods in each test class
- Test both success and failure scenarios
- Verify HTTP status codes, response structure, and data values

**How testing works in this project:**

```java
@SpringBootTest                              // Loads the full Spring context
@AutoConfigureMockMvc(addFilters = false)    // Disables JWT security for tests
public class RestaurantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;        // Simulates HTTP requests without starting a real server

    @Autowired
    private ObjectMapper objectMapper;  // Converts Java objects ↔ JSON
}
```

**Key MockMvc methods:**

| Method | Purpose |
|--------|---------|
| `mockMvc.perform(get("/path"))` | Simulate a GET request |
| `mockMvc.perform(post("/path"))` | Simulate a POST request |
| `mockMvc.perform(put("/path"))` | Simulate a PUT request |
| `mockMvc.perform(delete("/path"))` | Simulate a DELETE request |
| `.contentType(MediaType.APPLICATION_JSON)` | Set request Content-Type |
| `.content(jsonString)` | Set request body |
| `.param("key", "value")` | Add query parameter |
| `.andExpect(status().isOk())` | Assert HTTP 200 |
| `.andExpect(status().isCreated())` | Assert HTTP 201 |
| `.andExpect(status().isBadRequest())` | Assert HTTP 400 |
| `.andExpect(status().isNotFound())` | Assert HTTP 404 |
| `.andExpect(jsonPath("$.field").value(x))` | Assert JSON field value |
| `.andExpect(jsonPath("$.data").isArray())` | Assert field is an array |
| `.andExpect(jsonPath("$.data.id").isNumber())` | Assert field is a number |

**JsonPath syntax:**
- `$` → root of the response
- `$.message` → top-level "message" field
- `$.data.id` → "id" inside "data" object
- `$.data[0].name` → "name" of first item in "data" array

---

## Project Structure

```
src/main/java/com/rocketFoodDelivery/rocketFood/
├── controller/
│   ├── api/                    ← @RestController (JSON APIs) — Module 12
│   │   ├── AuthApiController
│   │   ├── RestaurantApiController
│   │   ├── ProductApiController
│   │   ├── OrderApiController
│   │   ├── OrderStatusApiController
│   │   └── ... (other API controllers)
│   └── backoffice/             ← @Controller (Thymeleaf) — Module 11
│       ├── RestaurantController
│       └── ...
├── dtos/                       ← Data Transfer Objects — NEW in Module 12
│   ├── restaurant/
│   ├── order/
│   ├── product/
│   ├── auth/
│   ├── response/
│   └── ...
├── models/                     ← JPA Entities (same as Module 11)
├── repository/                 ← JPA Repositories + Native SQL — EXTENDED in Module 12
├── service/                    ← Business Logic + DTO mapping — EXTENDED in Module 12
├── exception/                  ← Custom exceptions + global handler — NEW in Module 12
├── security/                   ← JWT + SecurityConfig — NEW in Module 12
└── util/                       ← ResponseBuilder — NEW in Module 12

src/test/java/com/rocketFoodDelivery/rocketFood/
└── api/                        ← Integration tests — NEW in Module 12
    ├── auth/
    ├── restaurant/
    ├── product/
    ├── order/
    └── ...
```

---

## Tips for Success

- Follow the implementation order: DTOs → Repository → Service → Controller → Tests
- Study the **already-implemented examples** (AuthApiController, OrderApiController, OrderStatusApiController) as reference
- Use `ResponseBuilder.buildOkResponse()` and `buildCreatedResponse()` for consistent responses
- Throw `ResourceNotFoundException` / `BadRequestException` — the global handler does the rest
- Use `@JsonProperty("snake_case")` in DTOs to match the expected API response format
- Test both **success** and **failure** scenarios for every endpoint
- Run tests with `./mvnw test` to verify your implementations
- The `mapRowToRestaurantDTO(Object[] row)` helper in RestaurantService shows how to convert native SQL results to DTOs

---

## Resources

- [Spring Boot REST API Guide](https://spring.io/guides/tutorials/rest/)
- [Spring Data JPA @Query](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods/at-query.html)
- [MockMvc Testing](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [JsonPath Syntax](https://github.com/json-path/JsonPath)
- [JWT Introduction](https://jwt.io/introduction)
- [Jackson @JsonProperty](https://github.com/FasterXML/jackson-annotations/wiki/Jackson-Annotations#property-naming)
- Database Schema: `db_schema_11_v2.txt` (same schema as Module 11)
