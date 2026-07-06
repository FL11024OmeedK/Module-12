package com.rocketFoodDelivery.rocketFood.api.product;

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
public class ProductApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/products ====================

    @Test
    public void testGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/products/{id} ====================

    @Test
    public void testGetProductById_Success() throws Exception {
        // todo: Send GET request to /api/products/{id} with a known valid id (e.g. 1)
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response contains "data.id" matching the requested id
        fail("todo: Implement test");
    }

    @Test
    public void testGetProductById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/products ====================

    @Test
    public void testCreateProduct_Success() throws Exception {
        // todo: Build a valid ApiCreateProductDTO (restaurant_id, name, description, cost)
        // todo: Send POST request to /api/products with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateProduct_Failure_MissingName() throws Exception {
        String body = "{\"cost\": 10, \"restaurant_id\": 1}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/products/{id} ====================

    @Test
    public void testUpdateProduct_Success() throws Exception {
        // todo: Build a valid ApiCreateProductDTO for update
        // todo: Send PUT request to /api/products/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateProduct_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiCreateProductDTO for update
        // todo: Send PUT request to /api/products/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/products/{id} ====================

    @Test
    public void testDeleteProduct_Success() throws Exception {
        // todo: Create a fresh product first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/products/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteProduct_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/products/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
