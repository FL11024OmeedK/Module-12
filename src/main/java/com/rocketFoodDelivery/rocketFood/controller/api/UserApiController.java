package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.user.ApiAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiCreateUserDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUpdateAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUserDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserApiController {
    private final UserService userService;


    public UserApiController(UserService userService) {
        this.userService = userService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/users - Get all users
    @GetMapping("/api/users")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseBuilder.buildOkResponse(userService.getAllUsersAsDtos());
    }


    // GET /api/users/{id} - Get user by ID
    @GetMapping("/api/users/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable int id) {
        ApiUserDTO user = userService.getUserByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(user);
    }


    // POST /api/users - Create new user
    @PostMapping("/api/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody ApiCreateUserDTO userDto) {
        ApiUserDTO created = userService.createUser(userDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/users/{id} - Update user by ID
    @PutMapping("/api/users/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable int id, @Valid @RequestBody ApiCreateUserDTO userDto) {
        ApiUserDTO updated = userService.updateUser(id, userDto)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/users/{id} - Delete user by ID
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiUserDTO user = userService.getUserByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
        userService.deleteUser(id);
        return ResponseBuilder.buildOkResponse(user);
    }


    // ==================== Custom Endpoints ====================


    // --- Get account details for a user ---
    @GetMapping("/api/account/{id}")
    public ResponseEntity<Object> getAccount(@PathVariable int id) {
        ApiAccountDTO dto = userService.getAccountDTO(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with id %d not found", id)));
        return ResponseBuilder.buildOkResponse(dto);
    }


    // --- Update account details for a user ---
    @PutMapping("/api/account/{id}")
    public ResponseEntity<Object> updateAccount(
            @PathVariable int id,
            @RequestParam(name = "type") String type,
            @RequestBody ApiUpdateAccountDTO updateDTO) {
        if (!type.equals("customer") && !type.equals("courier") && !type.equals("employee")) {
            throw new BadRequestException("Type must be 'customer', 'courier', or 'employee'");
        }
        ApiAccountDTO dto = userService.updateAccount(id, type, updateDTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %d or %s role not found", id, type)));
        return ResponseBuilder.buildOkResponse(dto);
    }
}
