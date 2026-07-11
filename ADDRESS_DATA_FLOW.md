The simplest representative path is:

```http
GET /api/addresses/7
Authorization: Bearer <JWT>
```

This request is useful because it passes through nearly every major project layer without the extra complexity of creating an order.

I’ll assume the database contains:

```text
id:             7
street_address: 123 Main Street
city:           Toronto
postal_code:    M1A 2B3
created_on:     2026-07-10 10:00
update_on:      2026-07-10 10:00
```

## 0. Application startup

File: [RocketFoodApplication.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/RocketFoodApplication.java)

```java
@SpringBootApplication
public class RocketFoodApplication {
    public static void main(String[] args) {
        SpringApplication.run(RocketFoodApplication.class, args);
    }
}
```

No address request exists yet. This code starts Spring and causes it to scan the project packages.

Spring discovers and constructs objects marked with annotations such as:

- `@RestController`
- `@Service`
- `@Repository`
- `@Configuration`
- `@Component`

It also connects dependencies:

```text
AddressApiController
        ↓ contains
AddressService
        ↓ contains
AddressRepository
```

The important detail is that the application creates these objects once at startup. It does not reconstruct every layer whenever a request arrives.

State at this point:

```text
HTTP request:       none
Address ID:         none
Address entity:     none
Address DTO:        none
HTTP response:      none
Spring application: running
```

## 1. The HTTP request enters the application

Assume a client sends:

```http
GET /api/addresses/7 HTTP/1.1
Host: localhost:8081
Authorization: Bearer eyJ...
```

State:

```text
HTTP method: GET
URI:         /api/addresses/7
Path data:   "7"
Header data: Authorization = "Bearer eyJ..."
Body:        none
```

Because the URI starts with `/api/`, it is handled by the API security configuration.

## 2. Security configuration selects the API filter chain

File: [SecurityConfig.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/security/SecurityConfig.java)

The relevant method is:

```java
@Bean
@Order(1)
public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(
            jwtTokenFilter,
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

The request data is examined here for the first time:

```text
/api/addresses/7
       ↓ matches
/api/**
```

Because this is not `/api/auth`, the request must be authenticated.

The address ID is still just part of the URL. It has not yet been converted into an integer or used to query the database.

State:

```text
HTTP method:            GET
URI:                    /api/addresses/7
Address ID:             still text inside URI
Authorization header:  Bearer eyJ...
Authenticated user:    not established yet
Address data:           none
```

## 3. JWT filter receives the request

File: [JwtTokenFilter.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/security/JwtTokenFilter.java)

Spring calls:

```java
doFilterInternal(request, response, filterChain)
```

At this moment:

```text
request  = HttpServletRequest containing URL and headers
response = empty HttpServletResponse
```

### 3.1 The request touches `JwtTokenFilter` the first time

The filter checks for a bearer header:

```java
if (!hasAuthorizationBearer(request)) {
    filterChain.doFilter(request, response);
    return;
}
```

That calls another method in the same file:

```java
private boolean hasAuthorizationBearer(HttpServletRequest request) {
    String header = request.getHeader("Authorization");

    if (ObjectUtils.isEmpty(header) || !header.startsWith("Bearer")) {
        return false;
    }

    return true;
}
```

This is the first intentional redundancy: the same `request` object moves from `doFilterInternal()` into `hasAuthorizationBearer()`.

State inside that method:

```text
request.Authorization = "Bearer eyJ..."
header                = "Bearer eyJ..."
return value          = true
```

The request then returns to `doFilterInternal()`.

### 3.2 The request touches `JwtTokenFilter` a second time

The filter extracts the token:

```java
String token = getAccessToken(request);
```

The request moves into another method in the same file:

```java
private String getAccessToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    String token = header.split(" ")[1].trim();
    return token;
}
```

Notice that the authorization header is read again. This is another redundancy.

State transformation:

```text
Before:
"Bearer eyJ..."

After split:
["Bearer", "eyJ..."]

Returned token:
"eyJ..."
```

Control returns to `doFilterInternal()`:

```text
request = original request
token   = "eyJ..."
```

### 3.3 The token is validated

```java
if (!jwtUtil.validateAccessToken(token)) {
    filterChain.doFilter(request, response);
    return;
}
```

The token now touches `JwtUtil`, which checks its signature and expiration.

If valid:

```text
token validity: true
```

If invalid, no authenticated user is placed into the security context, and Spring Security rejects access later.

### 3.4 The request and token touch `JwtTokenFilter` again

```java
setAuthenticationContext(token, request);
```

The token is passed into:

```java
private void setAuthenticationContext(
        String token,
        HttpServletRequest request) {

    UserDetails userDetails = getUserDetails(token);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails, null, null);

    authentication.setDetails(
        new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext()
                         .setAuthentication(authentication);
}
```

The token moves again into `getUserDetails()`:

```java
private UserDetails getUserDetails(String token) {
    User userDetails = new User();
    String[] jwtSubject = jwtUtil.getSubject(token).split(",");

    userDetails.setId(Integer.parseInt(jwtSubject[0]));
    userDetails.setEmail(jwtSubject[1]);

    return userDetails;
}
```

Suppose the JWT subject is:

```text
42,user@example.com
```

State transformation:

```text
JWT token
   ↓ JwtUtil.getSubject()
"42,user@example.com"
   ↓ split(",")
["42", "user@example.com"]
   ↓
User object:
{
    id: 42,
    email: "user@example.com"
}
```

That `User` is stored through its `UserDetails` interface:

```java
UserDetails userDetails = getUserDetails(token);
```

Spring places the authentication object in `SecurityContextHolder`.

Current state:

```text
Address ID:          still text inside URI
Authenticated user: {
    id: 42,
    email: "user@example.com"
}
Address data:        none
```

### 3.5 The request touches the filter one final time

Back in `doFilterInternal()`:

```java
filterChain.doFilter(request, response);
```

The same original request proceeds toward the controller. The filter did not replace the request or add address information. It added authentication to Spring’s separate security context.

## 4. Spring routes the request to the controller

File: [AddressApiController.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/AddressApiController.java)

Spring finds:

```java
@GetMapping("/api/addresses/{id}")
public ResponseEntity<Object> getAddressById(@PathVariable int id)
```

The URL contains:

```text
/api/addresses/7
               ↑
              {id}
```

Spring converts the path text into a Java integer:

```text
Before:
"7" as URL text

After:
int id = 7
```

The method begins with:

```java
id = 7
```

There is still no `Address` object or DTO.

## 5. The controller sends the ID to the service

Still in `AddressApiController.java`:

```java
ApiAddressDTO address =
    addressService.getAddressByIdAsDto(id)
        .orElseThrow(() ->
            new ResourceNotFoundException(
                "Address with id " + id + " not found"));
```

State leaving the controller:

```text
id = 7
```

Expected state returning later:

```text
Optional<ApiAddressDTO>
```

The controller does not know SQL and does not ask the database directly.

## 6. The service receives the ID

File: [AddressService.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/service/AddressService.java)

Spring calls:

```java
public Optional<ApiAddressDTO> getAddressByIdAsDto(int id) {
    return addressRepository.findAddressById(id)
            .map(this::mapAddressToDTO);
}
```

State entering:

```text
id = 7
```

The service forwards the same value to the repository:

```java
addressRepository.findAddressById(7)
```

This forwarding can look redundant, but it maintains the project’s separation of concerns:

```text
Controller: HTTP decisions
Service:    application/business decisions
Repository: database decisions
```

## 7. The repository receives the ID

File: [AddressRepository.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/repository/AddressRepository.java)

The called method is:

```java
@Query(nativeQuery = true, value = """
    SELECT * FROM addresses WHERE id = :addressId
""")
Optional<Address> findAddressById(
    @Param("addressId") int addressId);
```

State transformation:

```text
Java:
addressId = 7

SQL after parameter binding:
SELECT * FROM addresses WHERE id = 7
```

The value is parameter-bound rather than pasted into the SQL string. That avoids malformed SQL and SQL injection through this parameter.

`AddressRepository` is only an interface. Spring Data creates the concrete implementation at runtime.

## 8. The database processes the query

The database receives:

```sql
SELECT * FROM addresses WHERE id = 7
```

Suppose it finds:

```text
id             = 7
street_address = "123 Main Street"
city           = "Toronto"
postal_code    = "M1A 2B3"
created_on     = 2026-07-10T10:00
update_on      = 2026-07-10T10:00
```

The database returns a row, not a Java object:

```text
Database row:
[7, "123 Main Street", "Toronto", "M1A 2B3",
 2026-07-10T10:00, 2026-07-10T10:00]
```

## 9. Hibernate maps the row into an entity

File defining that entity: [Address.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/models/Address.java)

Hibernate uses:

```java
@Entity
@Table(name = "addresses")
public class Address
```

The column mappings include:

```java
@Column(name = "street_address")
private String streetAddress;

@Column(name = "city")
private String city;

@Column(name = "postal_code")
private String postalCode;
```

The transformation is:

```text
Database column        Java field
---------------        ----------
id                     id
street_address         streetAddress
city                   city
postal_code            postalCode
created_on             createdOn
update_on              updateOn
```

The data is now an entity:

```java
Address {
    id = 7,
    streetAddress = "123 Main Street",
    city = "Toronto",
    postalCode = "M1A 2B3",
    createdOn = 2026-07-10T10:00,
    updateOn = 2026-07-10T10:00
}
```

The repository wraps it in an `Optional`:

```java
Optional<Address> {
    Address(...)
}
```

This is the first time the data touches `Address.java`.

It may appear to touch this file repeatedly because Hibernate calls the generated constructor, setters, and getters. Lombok generates these methods because of:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

Conceptually, the entity state remains the same during those calls.

## 10. The entity returns to `AddressService`

This is the second visit to the service method:

```java
addressRepository.findAddressById(id)
        .map(this::mapAddressToDTO);
```

The repository call has now finished.

State in `AddressService`:

```java
Optional<Address> {
    id = 7,
    streetAddress = "123 Main Street",
    city = "Toronto",
    postalCode = "M1A 2B3",
    createdOn = ...,
    updateOn = ...
}
```

Because the `Optional` is not empty, `.map(...)` calls another method in the same service file:

```java
private ApiAddressDTO mapAddressToDTO(Address address)
```

This is an important redundancy: the data touches `AddressService.java` once to enter `getAddressByIdAsDto()` and again when the service maps the entity.

## 11. The service converts the entity to a DTO

Still in [AddressService.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/service/AddressService.java):

```java
private ApiAddressDTO mapAddressToDTO(Address address) {
    ApiAddressDTO dto = new ApiAddressDTO();

    dto.setId(address.getId());
    dto.setStreetAddress(address.getStreetAddress());
    dto.setCity(address.getCity());
    dto.setPostalCode(address.getPostalCode());

    return dto;
}
```

This code reads the entity repeatedly:

```text
address.getId()
address.getStreetAddress()
address.getCity()
address.getPostalCode()
```

Each call touches the generated getter behavior associated with `Address.java`.

It then writes to the DTO repeatedly:

```text
dto.setId(...)
dto.setStreetAddress(...)
dto.setCity(...)
dto.setPostalCode(...)
```

File defining the DTO: [ApiAddressDTO.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/dtos/address/ApiAddressDTO.java)

Before mapping:

```java
ApiAddressDTO {
    id = 0,
    streetAddress = null,
    city = null,
    postalCode = null
}
```

After each line:

```text
After setId:
{
    id: 7
}

After setStreetAddress:
{
    id: 7,
    streetAddress: "123 Main Street"
}

After setCity:
{
    id: 7,
    streetAddress: "123 Main Street",
    city: "Toronto"
}

After setPostalCode:
{
    id: 7,
    streetAddress: "123 Main Street",
    city: "Toronto",
    postalCode: "M1A 2B3"
}
```

Notice what disappears:

```text
createdOn → omitted
updateOn  → omitted
```

The DTO intentionally exposes only API-facing data.

The returned value becomes:

```java
Optional<ApiAddressDTO> {
    id = 7,
    streetAddress = "123 Main Street",
    city = "Toronto",
    postalCode = "M1A 2B3"
}
```

## 12. The DTO returns to the controller

The data touches [AddressApiController.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/controller/api/AddressApiController.java) for the second time.

Recall:

```java
ApiAddressDTO address =
    addressService.getAddressByIdAsDto(id)
        .orElseThrow(...);
```

Because the `Optional` contains a DTO, `orElseThrow()` unwraps it.

State:

```java
address = ApiAddressDTO {
    id = 7,
    streetAddress = "123 Main Street",
    city = "Toronto",
    postalCode = "M1A 2B3"
}
```

No error is thrown.

The controller passes the same DTO onward:

```java
return ResponseBuilder.buildOkResponse(address);
```

## 13. ResponseBuilder wraps the DTO

File: [ResponseBuilder.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/util/ResponseBuilder.java)

The method receives the address using the broad `Object` type:

```java
public static ResponseEntity<Object> buildOkResponse(Object data)
```

Runtime state:

```text
Declared parameter type: Object
Actual object type:      ApiAddressDTO
```

It creates a wrapper:

```java
ApiResponseDTO response = new ApiResponseDTO();
```

File defining the wrapper: [ApiResponseDTO.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/dtos/response/ApiResponseDTO.java)

Initial state:

```java
ApiResponseDTO {
    message = null,
    data = null
}
```

Then:

```java
response.setMessage("Success");
```

State:

```java
ApiResponseDTO {
    message = "Success",
    data = null
}
```

Then:

```java
response.setData(data);
```

State:

```java
ApiResponseDTO {
    message = "Success",
    data = ApiAddressDTO {
        id = 7,
        streetAddress = "123 Main Street",
        city = "Toronto",
        postalCode = "M1A 2B3"
    }
}
```

Then it creates the HTTP response:

```java
return new ResponseEntity<>(response, HttpStatus.OK);
```

Final Java response state:

```text
HTTP status: 200 OK

Body object:
ApiResponseDTO {
    message: "Success",
    data: ApiAddressDTO {...}
}
```

## 14. The response returns to the controller

The constructed `ResponseEntity<Object>` returns to the original controller method.

This is the third conceptual touch of `AddressApiController.java`:

1. Controller receives the request.
2. Controller receives the service’s DTO.
3. Controller returns the completed `ResponseEntity`.

No additional transformation occurs here.

## 15. Jackson converts the objects to JSON

Spring/Jackson serializes the response wrapper.

While serializing, Jackson touches:

- `ApiResponseDTO.getMessage()`
- `ApiResponseDTO.getData()`
- `ApiAddressDTO.getId()`
- `ApiAddressDTO.getStreetAddress()`
- `ApiAddressDTO.getCity()`
- `ApiAddressDTO.getPostalCode()`

The DTO annotations rename two Java properties:

```java
@JsonProperty("street_address")
private String streetAddress;

@JsonProperty("postal_code")
private String postalCode;
```

Transformation:

```text
Java property       JSON property
-------------       -------------
streetAddress       street_address
postalCode          postal_code
```

The Java object:

```java
ApiResponseDTO {
    message = "Success",
    data = ApiAddressDTO {
        id = 7,
        streetAddress = "123 Main Street",
        city = "Toronto",
        postalCode = "M1A 2B3"
    }
}
```

becomes:

```json
{
  "message": "Success",
  "data": {
    "id": 7,
    "street_address": "123 Main Street",
    "city": "Toronto",
    "postal_code": "M1A 2B3"
  }
}
```

## 16. The response passes back through the filter chain

The response travels backward through Spring’s filter chain, including `JwtTokenFilter`.

The filter does not modify the response, but execution returns to the point after:

```java
filterChain.doFilter(request, response);
```

This is another touchpoint in `JwtTokenFilter.java`, though the address DTO is normally stored in the HTTP response rather than handled directly by the filter.

Final HTTP state:

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "Success",
  "data": {
    "id": 7,
    "street_address": "123 Main Street",
    "city": "Toronto",
    "postal_code": "M1A 2B3"
  }
}
```

## Complete successful flow

```text
Client
  │
  │ GET /api/addresses/7 + JWT
  ▼
SecurityConfig.java
  │ selects /api/** filter chain
  ▼
JwtTokenFilter.java
  │ request → header → token → authenticated User
  │ request touches this file several times
  ▼
AddressApiController.java
  │ URL "7" → int 7
  ▼
AddressService.java
  │ forwards id 7
  ▼
AddressRepository.java
  │ id 7 → SQL parameter
  ▼
Database
  │ row containing six columns
  ▼
Address.java
  │ database row → Address entity
  ▼
AddressRepository.java
  │ Address → Optional<Address>
  ▼
AddressService.java
  │ Address → ApiAddressDTO
  │ timestamps removed
  ▼
ApiAddressDTO.java
  │ API-safe address state
  ▼
AddressApiController.java
  │ Optional unwrapped
  ▼
ResponseBuilder.java
  │ DTO → ApiResponseDTO → ResponseEntity
  ▼
ApiResponseDTO.java
  │ message + data
  ▼
Jackson
  │ Java names → JSON names
  ▼
JwtTokenFilter.java
  │ response passes back through filter chain
  ▼
Client receives 200 JSON
```

## Repeated file touches

| File | Why data touches it repeatedly |
|---|---|
| `JwtTokenFilter.java` | Checks header, extracts token, constructs authentication, forwards request, then receives returning response |
| `AddressApiController.java` | Receives request, receives DTO from service, returns HTTP response |
| `AddressService.java` | Receives ID, receives entity from repository, calls its own mapping method, returns DTO |
| `AddressRepository.java` | Receives ID for query and returns the mapped entity |
| `Address.java` | Hibernate populates it, then service calls several getters |
| `ApiAddressDTO.java` | Service calls several setters, then Jackson calls several getters |
| `ApiResponseDTO.java` | ResponseBuilder calls setters, then Jackson calls getters |

## If address 7 does not exist

The flow is identical through the database, but the database returns no row:

```text
Database result: empty
```

Repository state:

```java
Optional.empty()
```

Service state:

```java
Optional.empty()
```

Controller code:

```java
.orElseThrow(() ->
    new ResourceNotFoundException(
        "Address with id " + id + " not found"))
```

throws:

```text
ResourceNotFoundException:
"Address with id 7 not found"
```

That exception touches [ApiExceptionHandler.java](/home/omeedkashef/Module12/src/main/java/com/rocketFoodDelivery/rocketFood/exception/ApiExceptionHandler.java), which converts it into:

```http
HTTP/1.1 404 Not Found
Content-Type: application/json
```

```json
{
  "error": "Not Found",
  "details": "Address with id 7 not found"
}
```

So even the unsuccessful path preserves the same overall architecture:

```text
Controller → Service → Repository → Database
                                  empty result
Controller exception ← Service ← Repository
        ↓
Exception handler
        ↓
404 JSON response
```

The essential project flow is therefore:

> Security decides whether the request may continue; the controller translates HTTP data into Java data; the service coordinates application behavior; the repository translates Java calls into database queries; the entity represents complete database state; the DTO exposes selected state; and the response builder translates that DTO into the project’s standard HTTP response structure.
