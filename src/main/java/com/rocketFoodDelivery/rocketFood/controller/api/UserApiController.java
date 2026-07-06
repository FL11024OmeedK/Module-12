package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.user.ApiAccountDTO;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiUpdateAccountDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.UserService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

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
    // todo: implement get all


    // GET /api/users/{id} - Get user by ID
    // todo: implement get by id endpoint


    // POST /api/users - Create new user
    // todo: implement create endpoint


    // PUT /api/users/{id} - Update user by ID
    // todo: implement update endpoint


    // DELETE /api/users/{id} - Delete user by ID
    // todo: implement delete endpoint


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
