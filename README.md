# Rocket Food Delivery API

A Spring Boot backend for a food-delivery platform (think Uber Eats / DoorDash). It exposes a full JWT-secured REST API for managing restaurants, menus, orders, customers, couriers and employees, plus a separate session-authenticated "backoffice" web console (server-rendered with Thymeleaf) for staff to manage that data through a browser instead of raw API calls.

This project was built as a learning exercise (Module 12) to practice designing a layered Spring Boot application (controllers → services → repositories → JPA entities), JWT authentication, request validation, centralized error handling, and automated API testing.

## Table of Contents

- [Project Description](#rocket-food-delivery-api)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Installation / Setup](#installation--setup)
- [Environment Variables](#environment-variables)
- [API Documentation](#api-documentation)
- [Author](#author)

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.4 (Spring Web, Spring Data JPA, Spring Security, Bean Validation)
- **Auth:** JSON Web Tokens (`io.jsonwebtoken` / `jjwt`) for the REST API, form-login sessions for the backoffice
- **Database:** MySQL 8 (via `mysql-connector-j`), managed with Hibernate/JPA (`ddl-auto=update`)
- **Server-rendered UI:** Thymeleaf (backoffice CRUD screens for every entity)
- **Utilities:** Lombok, JavaFaker (fake data seeding), Jetbrains annotations
- **Build tool:** Maven (via the included `mvnw` / `mvnw.cmd` wrapper — no local Maven install required)
- **Testing:** JUnit 5 + Spring Boot Test (`spring-boot-starter-test`), integration tests for every controller
- **Manual API testing:** Postman collection included at [`PostmanCollection.json`](PostmanCollection.json)

## Project Structure

```
Module12/
├── src/
│   ├── main/
│   │   ├── java/com/rocketFoodDelivery/rocketFood/
│   │   │   ├── controller/
│   │   │   │   ├── api/           # REST controllers (JSON, JWT-secured) — e.g. RestaurantApiController
│   │   │   │   └── backoffice/    # Thymeleaf/session-secured staff console controllers
│   │   │   ├── dtos/              # Request/response DTOs, grouped by entity (auth, order, product, ...)
│   │   │   ├── exception/         # Custom exceptions + centralized error handling
│   │   │   ├── models/            # JPA entities (User, Restaurant, Order, Product, Courier, ...)
│   │   │   ├── repository/        # Spring Data JPA repositories
│   │   │   ├── security/          # JWT filter/util + Spring Security configuration
│   │   │   ├── service/           # Business logic, one service per entity
│   │   │   ├── util/              # Shared helpers (e.g. standard success response builder)
│   │   │   ├── DataSeeder.java    # Seeds the database with fake data on startup
│   │   │   └── RocketFoodApplication.java  # Application entry point
│   │   └── resources/
│   │       ├── application.properties      # DB connection, JPA, JWT and seeding config
│   │       └── templates/                  # Thymeleaf HTML views for the backoffice console
│   └── test/
│       └── java/.../api/          # Integration tests, one suite per REST controller
├── PostmanCollection.json         # Importable Postman collection covering every endpoint
├── pom.xml                        # Maven build file and dependencies
├── mvnw / mvnw.cmd                # Maven wrapper scripts (no local Maven needed)
└── CONCEPTS.md                    # Notes on challenging concepts encountered while building this
```

## Installation / Setup

**Prerequisites:**
- Java 21 (JDK)
- A running MySQL 8 server
- Nothing else — Maven itself is not required since the project ships with the Maven wrapper (`mvnw`)

**Steps:**

```bash
# Clone the repository
git clone git@github.com:FL11024OmeedK/Module-12.git

# Navigate to the project directory
cd Module-12

# Create the database the application expects
mysql -u root -p -e "CREATE DATABASE rdelivery;"

# Configure your database credentials
# Edit src/main/resources/application.properties and update
# spring.datasource.username / spring.datasource.password to match your local MySQL setup

# Run the application (downloads dependencies automatically on first run)
./mvnw spring-boot:run
# On Windows: mvnw.cmd spring-boot:run
```

The API will be available at `http://localhost:8080`, and the backoffice console at `http://localhost:8080/backoffice` (login page at `http://localhost:8080/backoffice/login`).

On first startup, `DataSeeder` automatically populates the database with fake restaurants, products, orders, customers and couriers (it skips seeding if data already exists). Three convenient test logins are seeded for exercising the API (all use the password `password`):

| Email | Role |
|---|---|
| `customer@gmail.com` | Customer |
| `courier@gmail.com` | Courier |
| `both@gmail.com` | Customer + Courier |

**Running tests:**

```bash
./mvnw test
```

## Environment Variables

This project does not use a `.env` file. Configuration instead lives directly in [`src/main/resources/application.properties`](src/main/resources/application.properties):

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/rdelivery
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# JWT signing secret used to sign/verify API auth tokens
app.jwt.secret=your_jwt_secret
```

> ⚠️ Note: the checked-in `application.properties` contains development-only defaults (including a plaintext DB password and JWT secret) for local convenience. Replace these with your own values before running against any real data, and never commit real production credentials.

## API Documentation

All REST endpoints are prefixed with `/api` and return JSON in the shape `{ "message": "...", "data": ... }` on success, or `{ "error": "...", "details": "..." }` on failure. Every endpoint except `POST /api/auth` requires a valid JWT sent as an `Authorization: Bearer <token>` header.

A ready-to-import request collection covering every endpoint (happy paths and error cases) is provided at [`PostmanCollection.json`](PostmanCollection.json) — import it into Postman, run "Auth - Login" first to populate the `{{token}}` variable, then run any other request.

### Authentication
```
POST   /api/auth                    - Log in with { email, password }, returns a JWT + user/customer/courier IDs
```

### Users & Accounts
```
GET    /api/users                   - List all users
GET    /api/users/{id}              - Get user by ID
POST   /api/users                   - Create a user
PUT    /api/users/{id}              - Update a user
DELETE /api/users/{id}              - Delete a user
GET    /api/account/{id}            - Get the account profile (user + role info) for a user ID
PUT    /api/account/{id}            - Update the account profile for a user ID
```

### Addresses
```
GET    /api/addresses               - List all addresses
GET    /api/addresses/{id}          - Get address by ID
POST   /api/addresses               - Create an address
PUT    /api/addresses/{id}          - Update an address
DELETE /api/addresses/{id}          - Delete an address
```

### Restaurants
```
GET    /api/restaurants             - List all restaurants
GET    /api/restaurants/{id}        - Get restaurant by ID
POST   /api/restaurants             - Create a restaurant
PUT    /api/restaurants/{id}        - Update a restaurant
DELETE /api/restaurants/{id}        - Delete a restaurant
```

### Products
```
GET    /api/products                - List all products
GET    /api/products/{id}           - Get product by ID
POST   /api/products                - Create a product
PUT    /api/products/{id}           - Update a product
DELETE /api/products/{id}           - Delete a product
```

### Customers
```
GET    /api/customers               - List all customers
GET    /api/customers/{id}          - Get customer by ID
POST   /api/customers               - Create a customer
PUT    /api/customers/{id}          - Update a customer
DELETE /api/customers/{id}          - Delete a customer
```

### Couriers & Courier Statuses
```
GET    /api/couriers                - List all couriers
GET    /api/couriers/{id}           - Get courier by ID
POST   /api/couriers                - Create a courier
PUT    /api/couriers/{id}           - Update a courier
DELETE /api/couriers/{id}           - Delete a courier

GET    /api/courier-statuses        - List all courier statuses
GET    /api/courier-statuses/{id}   - Get courier status by ID
POST   /api/courier-statuses        - Create a courier status
PUT    /api/courier-statuses/{id}   - Update a courier status
DELETE /api/courier-statuses/{id}   - Delete a courier status
```

### Employees
```
GET    /api/employees               - List all employees
GET    /api/employees/{id}          - Get employee by ID
POST   /api/employees               - Create an employee
PUT    /api/employees/{id}          - Update an employee
DELETE /api/employees/{id}          - Delete an employee
```

### Orders & Order Statuses
```
GET    /api/orders                  - List all orders
POST   /api/orders                  - Create an order
PUT    /api/orders/{id}             - Update an order
DELETE /api/orders/{id}             - Delete an order
PUT    /api/order/{id}/courier      - Assign a courier to an order
PUT    /api/order/{id}/rating       - Rate a delivered order

GET    /api/order-statuses          - List all order statuses
GET    /api/order-statuses/{id}     - Get order status by ID
POST   /api/order-statuses          - Create an order status
PUT    /api/order-statuses/{id}     - Update an order status
DELETE /api/order-statuses/{id}     - Delete an order status
POST   /api/order/{order_id}/status - Set the status of an order
```

### Product Orders (order line items)
```
GET    /api/product-orders          - List all product orders
GET    /api/product-orders/{id}     - Get product order by ID
POST   /api/product-orders          - Create a product order
PUT    /api/product-orders/{id}     - Update a product order
DELETE /api/product-orders/{id}     - Delete a product order
```

### Backoffice (staff web console)

In addition to the JSON API above, `/backoffice/**` serves a server-rendered (Thymeleaf) CRUD interface — with its own session/form-login authentication at `/backoffice/login` — for staff to manage restaurants, products, orders, users, customers, couriers, and their statuses directly from a browser.

## Author

**Omeed Kashef**
- GitHub: [@FL11024OmeedK](https://github.com/FL11024OmeedK)
- LinkedIn: [linkedin.com/in/omeedkashef](https://www.linkedin.com/in/omeedkashef/)
- Email: omeedkashef@gmail.com
