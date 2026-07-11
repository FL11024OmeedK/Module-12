# Module 12 – Rocket Delivery Helper

## 🎯 Purpose

This log explains three challenging concepts that came up while building the Rocket Delivery Helper project. Each one connects to real code in the application instead of being just a definition, so it shows how the idea is actually being used in the API, service layer, and tests.

## 📝 How to Use the CONCEPTS.md Log

1. Write the **`🔤 Name`** of the concept you found challenging.
2. Describe its **`🎯 Purpose`** within the project.
3. Explain in your own words **`❓ Why`** it was challenging
4. If applicable, indicate **`📍 Where`** it was used in your project (file name and line number).

---

## ✏️ Concept - 01

**🔤 Name:**

Consistent API contracts / centralized error handling

**🎯 Purpose:**

Every `/api/**` endpoint needs to respond in a predictable shape so any client (Postman, a future frontend, another service) can parse it without special-casing each endpoint: `{ "message": ..., "data": ... }` on success, `{ "error": ..., "details": ... }` on failure. In this project, that consistency matters because there are many API controllers for orders, users, products, customers, restaurants, couriers, statuses, and more. If each controller created responses in its own style, the API would become hard to test and hard for a client to use.

`ResponseBuilder` wraps every successful response the same way, and `ApiExceptionHandler` catches every exception thrown anywhere in the `controller.api` package and turns it into the same error shape. That means no individual controller method has to build its own try/catch or format its own error JSON. A controller can focus on the actual endpoint behavior, such as creating an order or assigning a courier, while the shared response utilities keep the outward-facing API contract clean and predictable.

**❓ Why it was challenging:**

The idea itself is simple once you see it ("just wrap every response the same way"), but getting there required understanding *where* to put that logic so it applies globally instead of being copy-pasted into every controller method. At first, it can feel natural to handle errors right inside each endpoint, because that is where the request is being processed. The challenging part was realizing that doing this would repeat the same response-building code over and over and make future changes harder.

Realizing that `@RestControllerAdvice` intercepts exceptions from an entire package, and that Spring picks the most specific matching `@ExceptionHandler` for whatever gets thrown, took some experimentation. For example, a missing order can become a `ResourceNotFoundException` and return a 404-style JSON response, while bad input can become a `BadRequestException` or validation error and return a 400-style JSON response. The fallback `handleGeneral(Exception ex)` catch-all is also important because it makes sure nothing ever leaks an unformatted stack trace back to the client. That made centralized error handling feel less like extra code and more like a safety net for the whole API.

**📍 Where (file & line):**

- [`ResponseBuilder.java:13-27`](src/main/java/com/rocketFoodDelivery/rocketFood/util/ResponseBuilder.java) — `buildOkResponse` / `buildCreatedResponse`
- [`ApiExceptionHandler.java:15-79`](src/main/java/com/rocketFoodDelivery/rocketFood/exception/ApiExceptionHandler.java) — `@RestControllerAdvice` with one `@ExceptionHandler` per exception type

---

## ✏️ Concept - 02

**🔤 Name:**

Layered architecture (Controller → Service → Repository)

**🎯 Purpose:**

Splitting each feature into a Controller (handles HTTP request/response), a Service (business logic, entity ↔ DTO mapping), and a Repository (database access) keeps each layer responsible for one thing. In this project, the controller should know about routes like `POST /api/orders` or form submissions from the backoffice, but it should not be responsible for all the rules of creating an order. The repository should know how to fetch and save database records, but it should not decide whether a product belongs to the selected restaurant or whether an order starts as `pending`.

The concrete payoff shows up with `OrderService`: it isn't just called by the JSON API controller, it's also called by the backoffice's session-authenticated web controller. That means the create/update/delete-order logic only had to be written once and both entry points stay in sync automatically. `OrderService` also becomes the place where the project handles more complicated order behavior, like checking restaurant and customer IDs, validating product quantities, preventing duplicate products in the same order request, saving line items, mapping entities into `ApiOrderDTO`, assigning couriers, updating ratings, and deleting orders.

**❓ Why it was challenging:**

It's easy to explain in the abstract, but it wasn't obvious at first *why* it mattered until seeing two different controllers depend on the exact same service. Before that, it was tempting to just put database queries and validation directly inside a controller method since it "worked" for a single endpoint. The real lesson was recognizing the duplication that would have resulted the moment a second controller (the backoffice one) needed the same order-creation logic with a different authentication model and response format.

This was also challenging because the layers are connected, so a change in one layer can still affect the others. For example, if the service changes what fields are included in `ApiOrderDTO`, the API response and tests may both need to reflect that. If a repository query changes, the service might still compile but return different data. The architecture helps organize the project, but it also requires discipline: controllers should stay thin, services should hold the business decisions, and repositories should stay focused on persistence.

**📍 Where (file & line):**

- [`OrderApiController.java:21-25`](src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/OrderApiController.java) — REST controller, injects `OrderService`
- [`OrderController.java:30-33`](src/main/java/com/rocketFoodDelivery/rocketFood/controller/backoffice/OrderController.java) — backoffice controller, injects the same `OrderService`
- [`OrderService.java`](src/main/java/com/rocketFoodDelivery/rocketFood/service/OrderService.java) — the shared business logic both controllers call into

---

## ✏️ Concept - 03

**🔤 Name:**

Testing philosophy (integration vs. unit)

**🎯 Purpose:**

`OrderApiControllerTest` uses `@SpringBootTest` and `MockMvc` to boot the real Spring context and drive actual HTTP requests through the full stack (security filter → controller → service → repository → database) and assert on the resulting status code and JSON body. This is different from a unit test, which would call `OrderService` directly with mocked repositories and check only the business logic in isolation, without involving Spring, HTTP, or the database at all.

In this project, the integration tests are especially useful because the API behavior depends on several moving parts working together. A request has to pass through security, match the correct route, deserialize the JSON request body, trigger validation, call the right service method, use repository data correctly, and return the standardized response shape. Testing with `MockMvc` gives confidence that the application behaves correctly from the outside, which is close to how Postman or a real frontend would use it.

**❓ Why it was challenging:**

The distinction itself — "integration tests exercise the real stack end-to-end, unit tests isolate one class" — is simple to state. What was harder to internalize is what an integration test in this project actually does and doesn't prove: `OrderApiControllerTest` confirms that a request to `POST /api/orders` returns the right status and JSON shape, but it doesn't pinpoint *which* layer would be at fault if something broke, the way a focused unit test on `OrderService` alone would.

That makes integration tests powerful but sometimes harder to debug. A failing order API test might be caused by the JWT/security setup, a controller route mismatch, invalid DTO validation, a service rule, missing seed data, a repository query, or even the expected JSON path in the test. Learning to read a failing integration test and reason backward through the layers (security → controller → service → repository) to find the actual cause was the real skill being built, not just knowing the vocabulary. The bigger takeaway is that integration tests and unit tests are not competing ideas. They answer different questions: "Does the whole request flow work?" versus "Does this one class handle its rules correctly?"

**📍 Where (file & line):**

- [`OrderApiControllerTest.java:22-28`](src/test/java/com/rocketFoodDelivery/rocketFood/api/order/OrderApiControllerTest.java) — `@SpringBootTest` + injected `MockMvc`
- [`OrderApiControllerTest.java:60-166`](src/test/java/com/rocketFoodDelivery/rocketFood/api/order/OrderApiControllerTest.java) — individual `@Test` methods driving `POST`/`GET`/`PUT`/`DELETE /api/orders` through `mockMvc.perform(...)`

---
