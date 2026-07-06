package com.rocketFoodDelivery.rocketFood.api.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class RestaurantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/restaurants ====================

    @Test
    public void testGetAllRestaurants_Success() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/restaurants/{id} ====================

    @Test
    public void testGetRestaurantById_Success() throws Exception {
        // todo: Send GET request to /api/restaurants/{id} with a known valid id (e.g. 1)
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response contains "data.id" matching the requested id
        fail("todo: Implement test");
    }

    @Test
    public void testGetRestaurantById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/restaurants/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/restaurants ====================

    @Test
    public void testCreateRestaurant_Success() throws Exception {
        // todo: Build a valid ApiCreateRestaurantDTO (user_id, name, price_range, phone, email)
        // todo: Include a valid ApiAddressDTO for the restaurant's address
        // todo: Send POST request to /api/restaurants with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateRestaurant_Failure_MissingAddress() throws Exception {
        // todo: Build an ApiCreateRestaurantDTO with null address
        // todo: Send POST request to /api/restaurants with JSON body
        // todo: Assert status 400 Bad Request
        fail("todo: Implement test");
    }

    // ==================== PUT /api/restaurants/{id} ====================

    @Test
    public void testUpdateRestaurant_Success() throws Exception {
        // todo: Build a valid ApiCreateRestaurantDTO for update
        // todo: Send PUT request to /api/restaurants/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateRestaurant_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiCreateRestaurantDTO for update
        // todo: Send PUT request to /api/restaurants/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/restaurants/{id} ====================

    @Test
    public void testDeleteRestaurant_Success() throws Exception {
        // todo: Create a fresh restaurant first (POST) with a valid address to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/restaurants/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteRestaurant_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/restaurants/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}