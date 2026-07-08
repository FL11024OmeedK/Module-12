package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiCreateRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.RestaurantService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RestaurantApiController {

    // Service dependency (constructor injection)
    private final RestaurantService restaurantService;

    public RestaurantApiController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // POST /api/restaurants - Create new restaurant
    @PostMapping("/api/restaurants")
    public ResponseEntity<Object> createRestaurant(@Valid @RequestBody ApiCreateRestaurantDTO restaurantDto) {
        ApiRestaurantDTO created = restaurantService.createRestaurant(restaurantDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // DELETE /api/restaurants/{id} - Delete restaurant by ID
    @DeleteMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> deleteRestaurant(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiRestaurantDTO restaurant = restaurantService.getRestaurantByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant with id " + id + " not found"));
        restaurantService.deleteRestaurant(id);
        return ResponseBuilder.buildOkResponse(restaurant);
    }


    // PUT /api/restaurants/{id} - Update restaurant by ID
    @PutMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> updateRestaurant(@PathVariable int id, @Valid @RequestBody ApiCreateRestaurantDTO restaurantDto) {
        ApiRestaurantDTO updated = restaurantService.updateRestaurant(id, restaurantDto)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // GET /api/restaurants/{id} - Get restaurant by ID
    @GetMapping("/api/restaurants/{id}")
    public ResponseEntity<Object> getRestaurantById(@PathVariable int id) {
        ApiRestaurantDTO restaurant = restaurantService.getRestaurantByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(restaurant);
    }


    // GET /api/restaurants - Get all restaurants (optionally filtered by rating and price_range)
    @GetMapping("/api/restaurants")
    public ResponseEntity<Object> getAllRestaurants(
            @RequestParam(name = "rating", required = false) Integer rating,
            @RequestParam(name = "price_range", required = false) Integer priceRange) {
        return ResponseBuilder.buildOkResponse(restaurantService.getRestaurantsAsDtos(rating, priceRange));
    }
}
