# Rocket Delivery API Reference — Module 12

Complete API endpoint reference with request/response formats for **all entities**.

---

## Table of Contents

1. [Authentication](#1-authentication)
2. [Users](#2-users)
3. [Accounts](#3-accounts)
4. [Addresses](#4-addresses)
5. [Restaurants](#5-restaurants)
6. [Products](#6-products)
7. [Customers](#7-customers)
8. [Couriers](#8-couriers)
9. [Courier Statuses](#9-courier-statuses)
10. [Employees](#10-employees)
11. [Orders](#11-orders)
12. [Order Statuses](#12-order-statuses)
13. [Product Orders](#13-product-orders)
14. [Error Response Format](#error-response-format)
15. [Testing with Postman](#testing-with-postman)

---

## 1. Authentication

### POST /api/auth

Authenticates a user and returns a JWT token. No token required for this endpoint.

**Request Body:**
```json
{
    "email": "john.doe@codeboxx.com",
    "password": "password"
}
```

**Response (200 OK):**
```json
{
    "success": true,
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "user_id": 1,
    "customer_id": 3,
    "courier_id": null
}
```

**Response (401 Unauthorized):**
```json
{
    "success": false
}
```

**Notes:**
- `customer_id` and `courier_id` are included if the user has those roles, `null` otherwise
- The returned `accessToken` must be sent as `Authorization: Bearer <token>` header on all other `/api/` endpoints

---

## 2. Users

### GET /api/users

Returns a list of all users.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "name": "John Doe",
            "email": "john.doe@codeboxx.com"
        }
    ]
}
```

---

### GET /api/users/{id}

Returns a single user by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | User ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@codeboxx.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "User with id 999 not found"
}
```

---

### POST /api/users

Creates a new user.

**Request Body:**
```json
{
    "name": "Jane Smith",
    "email": "jane.smith@example.com",
    "password": "securepassword"
}
```

**Validation rules:**
- `name` is required
- `email` is required and must be valid format
- `password` is required

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 31,
        "name": "Jane Smith",
        "email": "jane.smith@example.com"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/users/{id}

Updates an existing user.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | User ID |

**Request Body:**
```json
{
    "name": "Jane Updated",
    "email": "jane.updated@example.com",
    "password": "newpassword"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 31,
        "name": "Jane Updated",
        "email": "jane.updated@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "User with id 999 not found"
}
```

---

### DELETE /api/users/{id}

Deletes a user by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | User ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 31,
        "name": "Jane Smith",
        "email": "jane.smith@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "User with id 999 not found"
}
```

---

## 3. Accounts

### GET /api/account/{id}

Returns account details for a user, including their roles (customer, courier, employee) with associated contact info.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | User ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@codeboxx.com",
        "customer": {
            "id": 3,
            "phone": "+1-555-1234",
            "email": "john.customer@codeboxx.com",
            "address": "123 Main St, Montreal, H3G264"
        },
        "courier": {
            "id": 2,
            "phone": "+1-555-5678",
            "email": "john.courier@codeboxx.com",
            "address": "456 Oak Ave, Montreal, H3A1B1"
        },
        "employee": null
    }
}
```

**Notes:**
- Role fields (`customer`, `courier`, `employee`) are `null` if the user doesn't have that role
- `null` fields are excluded from JSON output via `@JsonInclude(NON_NULL)`

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "User with id 999 not found"
}
```

---

### PUT /api/account/{id}

Updates contact details for a specific role of a user.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | User ID |

**Query Parameters:**

| Parameter | Type | Required | Values | Description |
|-----------|------|----------|--------|-------------|
| `type` | String | Yes | `customer`, `courier`, `employee` | Which role to update |

**Example:**
```
PUT /api/account/1?type=customer
```

**Request Body:**
```json
{
    "email": "new.email@example.com",
    "phone": "+1-555-9999"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@codeboxx.com",
        "customer": {
            "id": 3,
            "phone": "+1-555-9999",
            "email": "new.email@example.com",
            "address": "123 Main St, Montreal, H3G264"
        }
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Type must be 'customer', 'courier', or 'employee'"
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "User with id 999 or customer role not found"
}
```

---

## 4. Addresses

### GET /api/addresses

Returns a list of all addresses.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "street_address": "123 Main St",
            "city": "Montreal",
            "postal_code": "H3G264"
        }
    ]
}
```

---

### GET /api/addresses/{id}

Returns a single address by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Address ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "street_address": "123 Main St",
        "city": "Montreal",
        "postal_code": "H3G264"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Address with id 999 not found"
}
```

---

### POST /api/addresses

Creates a new address.

**Request Body:**
```json
{
    "street_address": "789 Pine Blvd",
    "city": "Toronto",
    "postal_code": "M5V2T6"
}
```

**Validation rules:**
- `street_address` is required (not blank)
- `city` is required (not blank)
- `postal_code` is required (not blank)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 31,
        "street_address": "789 Pine Blvd",
        "city": "Toronto",
        "postal_code": "M5V2T6"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/addresses/{id}

Updates an existing address.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Address ID |

**Request Body:**
```json
{
    "street_address": "999 Updated Ave",
    "city": "Vancouver",
    "postal_code": "V6B1A1"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "street_address": "999 Updated Ave",
        "city": "Vancouver",
        "postal_code": "V6B1A1"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Address with id 999 not found"
}
```

---

### DELETE /api/addresses/{id}

Deletes an address by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Address ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "street_address": "123 Main St",
        "city": "Montreal",
        "postal_code": "H3G264"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Address with id 999 not found"
}
```

---

## 5. Restaurants

### GET /api/restaurants

Returns a list of restaurants, optionally filtered by rating and/or price range.

**Query Parameters:**

| Parameter | Type | Required | Values | Description |
|-----------|------|----------|--------|-------------|
| `rating` | Integer | No | 1-5 | Filter by average rating |
| `price_range` | Integer | No | 1-3 | Filter by price range |

**Examples:**
```
GET /api/restaurants
GET /api/restaurants?rating=5&price_range=1
GET /api/restaurants?rating=3
GET /api/restaurants?price_range=2
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "name": "Villa Wellington",
            "price_range": 3,
            "rating": 4
        },
        {
            "id": 2,
            "name": "Fast Pub",
            "price_range": 1,
            "rating": 3
        }
    ]
}
```

---

### GET /api/restaurants/{id}

Returns a single restaurant by ID with computed average rating.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Restaurant ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "Villa Wellington",
        "price_range": 3,
        "rating": 4
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Restaurant with id 999 not found"
}
```

---

### POST /api/restaurants

Creates a new restaurant with an associated address.

**Request Body:**
```json
{
    "user_id": 2,
    "name": "Villa Wellington",
    "phone": "15141234567",
    "email": "villa@wellington.com",
    "price_range": 2,
    "address": {
        "street_address": "123 Wellington St.",
        "city": "Montreal",
        "postal_code": "H3G264"
    }
}
```

**Validation rules:**
- `name` is required (not blank)
- `price_range` must be between 1 and 3
- `phone` is required (not blank)
- `email` must be valid format (optional)
- `user_id` must reference an existing user
- `address` object is required with `street_address`, `city`, `postal_code`

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 9,
        "name": "Villa Wellington",
        "phone": "15141234567",
        "email": "villa@wellington.com",
        "user_id": 2,
        "price_range": 2,
        "address": {
            "id": 21,
            "street_address": "123 Wellington St.",
            "city": "Montreal",
            "postal_code": "H3G264"
        }
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/restaurants/{id}

Modifies an existing restaurant. Can update: `name`, `price_range`, `phone`.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Restaurant ID |

**Request Body:**
```json
{
    "name": "B12 Nation",
    "price_range": 3,
    "phone": "2223334444"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 9,
        "name": "B12 Nation",
        "phone": "2223334444",
        "email": "villa@wellington.com",
        "address": {
            "id": 21,
            "city": "Montreal",
            "street_address": "123 Wellington St.",
            "postal_code": "H3G264"
        },
        "user_id": 3,
        "price_range": 3
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Restaurant with id 10 not found"
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Validation failed: price_range must be between 1 and 3"
}
```

---

### DELETE /api/restaurants/{id}

Deletes a restaurant by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Restaurant ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "Don Mole",
        "price_range": 2,
        "rating": 4
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Restaurant with id 942 not found"
}
```

---

## 6. Products

### GET /api/products

Returns a list of products, optionally filtered by restaurant.

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `restaurant` | Integer | No | Restaurant ID to filter products by |

**Examples:**
```
GET /api/products
GET /api/products?restaurant=5
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "name": "Cheeseburger",
            "cost": 525
        },
        {
            "id": 2,
            "name": "Vegetable Soup",
            "cost": 1975
        }
    ]
}
```

**Response (404 Not Found) - invalid restaurant:**
```json
{
    "error": "Not Found",
    "details": "Restaurant with id 999 not found"
}
```

---

### GET /api/products/{id}

Returns a single product by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "Cheeseburger",
        "description": "Classic beef cheeseburger",
        "cost": 525,
        "restaurant_id": 1
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product with id 999 not found"
}
```

---

### POST /api/products

Creates a new product.

**Request Body:**
```json
{
    "restaurant_id": 1,
    "name": "New Burger",
    "description": "A delicious new burger",
    "cost": 899
}
```

**Validation rules:**
- `name` is required (not blank)
- `cost` is required and must be >= 0
- `restaurant_id` is required and must reference an existing restaurant
- `description` is optional

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 51,
        "name": "New Burger",
        "description": "A delicious new burger",
        "cost": 899,
        "restaurant_id": 1
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/products/{id}

Updates an existing product.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product ID |

**Request Body:**
```json
{
    "name": "Updated Burger",
    "description": "An updated description",
    "cost": 999
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "Updated Burger",
        "description": "An updated description",
        "cost": 999,
        "restaurant_id": 1
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product with id 999 not found"
}
```

---

### DELETE /api/products/{id}

Deletes a product by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "Cheeseburger",
        "cost": 525
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product with id 999 not found"
}
```

---

## 7. Customers

### GET /api/customers

Returns a list of all customers.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "user_id": 5,
            "address_id": 3,
            "phone": "+1-555-1234",
            "email": "customer@example.com",
            "active": true
        }
    ]
}
```

---

### GET /api/customers/{id}

Returns a single customer by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Customer ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 5,
        "address_id": 3,
        "phone": "+1-555-1234",
        "email": "customer@example.com",
        "active": true
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Customer with id 999 not found"
}
```

---

### POST /api/customers

Creates a new customer.

**Request Body:**
```json
{
    "user_id": 5,
    "address_id": 3,
    "phone": "+1-555-1234",
    "email": "customer@example.com",
    "active": true
}
```

**Validation rules:**
- `user_id` is required (must reference existing user; one customer per user)
- `address_id` is required (must reference existing address)
- `phone` is required (not blank)
- `email` must be valid format (optional)
- `active` is required (defaults to `true`)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 9,
        "user_id": 5,
        "address_id": 3,
        "phone": "+1-555-1234",
        "email": "customer@example.com",
        "active": true
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/customers/{id}

Updates an existing customer.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Customer ID |

**Request Body:**
```json
{
    "user_id": 5,
    "address_id": 3,
    "phone": "+1-555-9999",
    "email": "updated@example.com",
    "active": true
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 5,
        "address_id": 3,
        "phone": "+1-555-9999",
        "email": "updated@example.com",
        "active": true
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Customer with id 999 not found"
}
```

---

### DELETE /api/customers/{id}

Deletes a customer by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Customer ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 5,
        "phone": "+1-555-1234",
        "email": "customer@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Customer with id 999 not found"
}
```

---

## 8. Couriers

### GET /api/couriers

Returns a list of all couriers.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "user_id": 10,
            "address_id": 7,
            "courier_status_id": 1,
            "phone": "+1-555-4567",
            "email": "courier@example.com",
            "active": true
        }
    ]
}
```

---

### GET /api/couriers/{id}

Returns a single courier by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 10,
        "address_id": 7,
        "courier_status_id": 1,
        "phone": "+1-555-4567",
        "email": "courier@example.com",
        "active": true
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier with id 999 not found"
}
```

---

### POST /api/couriers

Creates a new courier.

**Request Body:**
```json
{
    "user_id": 10,
    "address_id": 7,
    "courier_status_id": 1,
    "phone": "+1-555-4567",
    "email": "courier@example.com",
    "active": true
}
```

**Validation rules:**
- `user_id` is required (must reference existing user; one courier per user)
- `address_id` is required (must reference existing address)
- `courier_status_id` is required (must reference existing courier status)
- `phone` is required (not blank)
- `email` must be valid format (optional)
- `active` is required (defaults to `true`)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 9,
        "user_id": 10,
        "address_id": 7,
        "courier_status_id": 1,
        "phone": "+1-555-4567",
        "email": "courier@example.com",
        "active": true
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/couriers/{id}

Updates an existing courier.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier ID |

**Request Body:**
```json
{
    "user_id": 10,
    "address_id": 7,
    "courier_status_id": 2,
    "phone": "+1-555-0000",
    "email": "updated.courier@example.com",
    "active": true
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 10,
        "address_id": 7,
        "courier_status_id": 2,
        "phone": "+1-555-0000",
        "email": "updated.courier@example.com",
        "active": true
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier with id 999 not found"
}
```

---

### DELETE /api/couriers/{id}

Deletes a courier by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 10,
        "phone": "+1-555-4567",
        "email": "courier@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier with id 999 not found"
}
```

---

## 9. Courier Statuses

### GET /api/courier-statuses

Returns a list of all courier statuses.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        { "id": 1, "name": "free" },
        { "id": 2, "name": "busy" },
        { "id": 3, "name": "full" },
        { "id": 4, "name": "offline" }
    ]
}
```

---

### GET /api/courier-statuses/{id}

Returns a single courier status by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier Status ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "free"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier status with id 999 not found"
}
```

---

### POST /api/courier-statuses

Creates a new courier status.

**Request Body:**
```json
{
    "name": "on_break"
}
```

**Validation rules:**
- `name` is required (not blank)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 5,
        "name": "on_break"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/courier-statuses/{id}

Updates an existing courier status.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier Status ID |

**Request Body:**
```json
{
    "name": "updated_status"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "updated_status"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier status with id 999 not found"
}
```

---

### DELETE /api/courier-statuses/{id}

Deletes a courier status by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Courier Status ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 5,
        "name": "on_break"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Courier status with id 999 not found"
}
```

---

## 10. Employees

### GET /api/employees

Returns a list of all employees.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "user_id": 2,
            "address_id": 4,
            "phone": "+1-555-7890",
            "email": "employee@example.com"
        }
    ]
}
```

---

### GET /api/employees/{id}

Returns a single employee by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Employee ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 2,
        "address_id": 4,
        "phone": "+1-555-7890",
        "email": "employee@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Employee with id 999 not found"
}
```

---

### POST /api/employees

Creates a new employee.

**Request Body:**
```json
{
    "user_id": 2,
    "address_id": 4,
    "phone": "+1-555-7890",
    "email": "employee@example.com"
}
```

**Validation rules:**
- `user_id` is required (must reference existing user; one employee per user)
- `address_id` is required (must reference existing address)
- `phone` is required (not blank)
- `email` must be valid format (optional)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 6,
        "user_id": 2,
        "address_id": 4,
        "phone": "+1-555-7890",
        "email": "employee@example.com"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/employees/{id}

Updates an existing employee.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Employee ID |

**Request Body:**
```json
{
    "user_id": 2,
    "address_id": 4,
    "phone": "+1-555-0000",
    "email": "updated.employee@example.com"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 2,
        "address_id": 4,
        "phone": "+1-555-0000",
        "email": "updated.employee@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Employee with id 999 not found"
}
```

---

### DELETE /api/employees/{id}

Deletes an employee by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Employee ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "user_id": 2,
        "phone": "+1-555-7890",
        "email": "employee@example.com"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Employee with id 999 not found"
}
```

---

## 11. Orders

### GET /api/orders

Returns a list of orders filtered by user type and ID.

**Query Parameters:**

| Parameter | Type | Required | Values | Description |
|-----------|------|----------|--------|-------------|
| `type` | String | Yes | `customer`, `restaurant`, `courier` | Type of user to filter by |
| `id` | Integer | Yes | - | ID of the customer, restaurant, or courier (NOT the user table ID) |

**Example:**
```
GET /api/orders?type=customer&id=7
```

**Response (200 OK):**
```json
[
    {
        "id": 3,
        "customer_id": 5,
        "customer_name": "Cathy Spinka",
        "customer_address": "7757 Darwin Causeway, Gerlachfort, 19822",
        "restaurant_id": 1,
        "restaurant_name": "Fast Pub",
        "restaurant_address": "5398 Quigley Harbor, North Lynelle, 60808",
        "courier_id": 3,
        "courier_name": "Cathy Spinka",
        "status": "in progress",
        "products": [
            {
                "product_id": 2,
                "product_name": "Vegetable Soup",
                "quantity": 2,
                "unit_cost": 1975,
                "total_cost": 3950
            },
            {
                "product_id": 4,
                "product_name": "Peking Duck",
                "quantity": 1,
                "unit_cost": 175,
                "total_cost": 175
            }
        ],
        "total_cost": 5975,
        "created_on": "2026-04-06T10:30:00"
    }
]
```

**Response (400 Bad Request) - invalid type:**
```json
{
    "error": "Bad Request",
    "details": "Type must be 'restaurant' or 'customer' or 'courier'"
}
```

**Notes:**
- `id` refers to the entity's own ID (customers.id, restaurants.id, couriers.id), NOT the users.id
- `total_cost` for each product = `quantity x unit_cost`
- Order-level `total_cost` = sum of all product `total_cost` values
- `courier_id` / `courier_name` may be `null` if no courier assigned

---

### POST /api/orders

Creates a new order with products.

**Request Body:**
```json
{
    "restaurant_id": 1,
    "customer_id": 3,
    "products": [
        {
            "id": 2,
            "quantity": 1
        },
        {
            "id": 3,
            "quantity": 3
        }
    ]
}
```

**Validation rules:**
- `restaurant_id` must reference an existing restaurant
- `customer_id` must reference an existing customer
- `products` array is required and cannot be empty
- Each product must belong to the specified restaurant
- Cannot add the same product twice in one order

**Response (200 OK):** Same format as GET /api/orders response for a single order.

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/orders/{id}

Updates an existing order.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order ID |

**Request Body:**
```json
{
    "customer_id": 1,
    "restaurant_id": 1,
    "courier_id": 1
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": { "...": "..." }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order with id 999 not found"
}
```

---

### DELETE /api/orders/{id}

Deletes an order by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": { "...": "..." }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order with id 999 not found"
}
```

---

### PUT /api/order/{id}/courier

Assigns a courier to an existing order.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order ID |

**Request Body:**
```json
{
    "courier_id": 3
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 3,
        "customer_id": 5,
        "customer_name": "Cathy Spinka",
        "restaurant_id": 1,
        "restaurant_name": "Fast Pub",
        "courier_id": 3,
        "courier_name": "Delivery Driver",
        "status": "pending",
        "products": ["..."],
        "total_cost": 5975
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order with id 999 or courier not found"
}
```

---

### PUT /api/order/{id}/rating

Updates the restaurant rating on an order.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order ID |

**Request Body:**
```json
{
    "restaurant_rating": 4
}
```

**Validation:** Rating must be between 1 and 5.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 3,
        "customer_id": 5,
        "restaurant_id": 1,
        "status": "delivered",
        "products": ["..."],
        "total_cost": 5975
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order with id 999 not found"
}
```

---

## 12. Order Statuses

### POST /api/order/{order_id}/status (Custom Endpoint)

Changes the status of an existing order. This uses the order status **name** (not ID).

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `order_id` | Integer | Yes | Order ID |

**Request Body:**
```json
{
    "status": "delivered"
}
```

**Valid status values:** `pending`, `in progress`, `delivered`

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "status": "delivered"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Status is required"
}
```

**Response (400 Bad Request) - invalid status value:**
```json
{
    "error": "Bad Request",
    "details": "Invalid order id or status: nonexistent_status"
}
```

---

### GET /api/order-statuses

Returns a list of all order statuses.

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        { "id": 1, "name": "pending" },
        { "id": 2, "name": "in progress" },
        { "id": 3, "name": "delivered" }
    ]
}
```

---

### GET /api/order-statuses/{id}

Returns a single order status by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order Status ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "pending"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order status with id 999 not found"
}
```

---

### POST /api/order-statuses

Creates a new order status.

**Request Body:**
```json
{
    "name": "cancelled"
}
```

**Validation rules:**
- `name` is required (not blank)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 4,
        "name": "cancelled"
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/order-statuses/{id}

Updates an existing order status.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order Status ID |

**Request Body:**
```json
{
    "name": "updated_status"
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "name": "updated_status"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order status with id 999 not found"
}
```

---

### DELETE /api/order-statuses/{id}

Deletes an order status by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Order Status ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 4,
        "name": "cancelled"
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Order status with id 999 not found"
}
```

---

## 13. Product Orders

### GET /api/product-orders

Returns a list of all product orders (join table between products and orders).

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": [
        {
            "id": 1,
            "product_id": 2,
            "order_id": 3,
            "product_quantity": 2,
            "product_unit_cost": 1975
        }
    ]
}
```

---

### GET /api/product-orders/{id}

Returns a single product order by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product Order ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "product_id": 2,
        "order_id": 3,
        "product_quantity": 2,
        "product_unit_cost": 1975
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product order with id 999 not found"
}
```

---

### POST /api/product-orders

Creates a new product order entry.

**Request Body:**
```json
{
    "product_id": 2,
    "order_id": 3,
    "product_quantity": 2,
    "product_unit_cost": 1975
}
```

**Validation rules:**
- `product_id` is required (must reference existing product)
- `order_id` is required (must reference existing order)
- `product_quantity` is required (must be >= 1)
- `product_unit_cost` is required (must be >= 0)
- Product must belong to the same restaurant as the order
- Cannot add the same product twice to the same order (unique constraint)

**Response (201 Created):**
```json
{
    "message": "Success",
    "data": {
        "id": 10,
        "product_id": 2,
        "order_id": 3,
        "product_quantity": 2,
        "product_unit_cost": 1975
    }
}
```

**Response (400 Bad Request):**
```json
{
    "error": "Bad Request",
    "details": "Invalid or missing parameters"
}
```

---

### PUT /api/product-orders/{id}

Updates an existing product order.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product Order ID |

**Request Body:**
```json
{
    "product_id": 2,
    "order_id": 3,
    "product_quantity": 5,
    "product_unit_cost": 1975
}
```

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "product_id": 2,
        "order_id": 3,
        "product_quantity": 5,
        "product_unit_cost": 1975
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product order with id 999 not found"
}
```

---

### DELETE /api/product-orders/{id}

Deletes a product order by ID.

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | Integer | Yes | Product Order ID |

**Response (200 OK):**
```json
{
    "message": "Success",
    "data": {
        "id": 1,
        "product_id": 2,
        "order_id": 3,
        "product_quantity": 2,
        "product_unit_cost": 1975
    }
}
```

**Response (404 Not Found):**
```json
{
    "error": "Not Found",
    "details": "Product order with id 999 not found"
}
```

---

## Error Response Format

All API errors follow this consistent format:

```json
{
    "error": "<Error Type>",
    "details": "<Specific error message or null>"
}
```

**HTTP Status Codes Used:**

| Status Code | Meaning | When |
|-------------|---------|------|
| 200 OK | Success | GET, PUT, DELETE success |
| 201 Created | Resource created | POST success |
| 400 Bad Request | Invalid input | Missing/invalid parameters, validation failure |
| 401 Unauthorized | Auth failed | Wrong email/password, missing/invalid JWT |
| 404 Not Found | Resource missing | Entity with given ID doesn't exist |
| 500 Internal Server Error | Server error | Unexpected error |

---

## Complete Endpoint Summary

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| 1 | POST | `/api/auth` | Authenticate user, get JWT token |
| 2 | GET | `/api/users` | List all users |
| 3 | GET | `/api/users/{id}` | Get user by ID |
| 4 | POST | `/api/users` | Create user |
| 5 | PUT | `/api/users/{id}` | Update user |
| 6 | DELETE | `/api/users/{id}` | Delete user |
| 7 | GET | `/api/account/{id}` | Get user account with roles |
| 8 | PUT | `/api/account/{id}?type=` | Update role contact info |
| 9 | GET | `/api/addresses` | List all addresses |
| 10 | GET | `/api/addresses/{id}` | Get address by ID |
| 11 | POST | `/api/addresses` | Create address |
| 12 | PUT | `/api/addresses/{id}` | Update address |
| 13 | DELETE | `/api/addresses/{id}` | Delete address |
| 14 | GET | `/api/restaurants` | List restaurants (optional filters) |
| 15 | GET | `/api/restaurants/{id}` | Get restaurant by ID |
| 16 | POST | `/api/restaurants` | Create restaurant |
| 17 | PUT | `/api/restaurants/{id}` | Update restaurant |
| 18 | DELETE | `/api/restaurants/{id}` | Delete restaurant |
| 19 | GET | `/api/products` | List products (optional restaurant filter) |
| 20 | GET | `/api/products/{id}` | Get product by ID |
| 21 | POST | `/api/products` | Create product |
| 22 | PUT | `/api/products/{id}` | Update product |
| 23 | DELETE | `/api/products/{id}` | Delete product |
| 24 | GET | `/api/customers` | List all customers |
| 25 | GET | `/api/customers/{id}` | Get customer by ID |
| 26 | POST | `/api/customers` | Create customer |
| 27 | PUT | `/api/customers/{id}` | Update customer |
| 28 | DELETE | `/api/customers/{id}` | Delete customer |
| 29 | GET | `/api/couriers` | List all couriers |
| 30 | GET | `/api/couriers/{id}` | Get courier by ID |
| 31 | POST | `/api/couriers` | Create courier |
| 32 | PUT | `/api/couriers/{id}` | Update courier |
| 33 | DELETE | `/api/couriers/{id}` | Delete courier |
| 34 | GET | `/api/courier-statuses` | List all courier statuses |
| 35 | GET | `/api/courier-statuses/{id}` | Get courier status by ID |
| 36 | POST | `/api/courier-statuses` | Create courier status |
| 37 | PUT | `/api/courier-statuses/{id}` | Update courier status |
| 38 | DELETE | `/api/courier-statuses/{id}` | Delete courier status |
| 39 | GET | `/api/employees` | List all employees |
| 40 | GET | `/api/employees/{id}` | Get employee by ID |
| 41 | POST | `/api/employees` | Create employee |
| 42 | PUT | `/api/employees/{id}` | Update employee |
| 43 | DELETE | `/api/employees/{id}` | Delete employee |
| 44 | GET | `/api/orders?type=&id=` | List orders by type and ID |
| 45 | POST | `/api/orders` | Create order |
| 46 | PUT | `/api/orders/{id}` | Update order |
| 47 | DELETE | `/api/orders/{id}` | Delete order |
| 48 | PUT | `/api/order/{id}/courier` | Assign courier to order |
| 49 | PUT | `/api/order/{id}/rating` | Rate restaurant on order |
| 50 | POST | `/api/order/{order_id}/status` | Change order status |
| 51 | GET | `/api/order-statuses` | List all order statuses |
| 52 | GET | `/api/order-statuses/{id}` | Get order status by ID |
| 53 | POST | `/api/order-statuses` | Create order status |
| 54 | PUT | `/api/order-statuses/{id}` | Update order status |
| 55 | DELETE | `/api/order-statuses/{id}` | Delete order status |
| 56 | GET | `/api/product-orders` | List all product orders |
| 57 | GET | `/api/product-orders/{id}` | Get product order by ID |
| 58 | POST | `/api/product-orders` | Create product order |
| 59 | PUT | `/api/product-orders/{id}` | Update product order |
| 60 | DELETE | `/api/product-orders/{id}` | Delete product order |

---

## Testing with Postman

### Setup
1. Start your application (`./mvnw spring-boot:run`)
2. Base URL: `http://localhost:8080`

### Authentication Flow
1. Send `POST /api/auth` with valid credentials
2. Copy the `accessToken` from the response
3. In Postman, add header: `Authorization: Bearer <your_token>`
4. All subsequent `/api/*` requests will be authenticated

### Quick Test Sequence
1. `POST /api/auth` - get token
2. `GET /api/users` - list users
3. `GET /api/account/1` - verify account with roles
4. `GET /api/addresses` - list addresses
5. `POST /api/addresses` - create new address
6. `GET /api/restaurants` - list restaurants
7. `GET /api/restaurants/1` - get single restaurant with rating
8. `POST /api/restaurants` - create new restaurant (with address)
9. `PUT /api/restaurants/{id}` - update it
10. `DELETE /api/restaurants/{id}` - delete it
11. `GET /api/products?restaurant=1` - list products for restaurant
12. `POST /api/products` - create new product
13. `GET /api/customers` - list customers
14. `GET /api/couriers` - list couriers
15. `GET /api/employees` - list employees
16. `GET /api/orders?type=customer&id=1` - list orders
17. `POST /api/orders` - create new order with products
18. `PUT /api/order/1/courier` - assign courier
19. `PUT /api/order/1/rating` - rate the restaurant
20. `POST /api/order/1/status` - change order status
21. `GET /api/order-statuses` - list order statuses
22. `GET /api/courier-statuses` - list courier statuses
23. `GET /api/product-orders` - list product orders
