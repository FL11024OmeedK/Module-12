package com.rocketFoodDelivery.rocketFood.api.productOrder;

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
public class ProductOrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/product-orders ====================

    @Test
    public void testGetAllProductOrders_Success() throws Exception {
        mockMvc.perform(get("/api/product-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/product-orders/{id} ====================

    @Test
    public void testGetProductOrderById_Success() throws Exception {
        mockMvc.perform(get("/api/product-orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetProductOrderById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/product-orders/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/product-orders ====================

    @Test
    public void testCreateProductOrder_Success() throws Exception {
        // todo: Create a fresh product and a fresh order first (both for the same restaurant)
        // todo: Build a valid ApiProductOrderDTO (product_id, order_id, product_quantity, product_unit_cost)
        // todo: Send POST request to /api/product-orders with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateProductOrder_Failure_InvalidData() throws Exception {
        // todo: Build an invalid ApiProductOrderDTO (non-existent product/order ids)
        // todo: Send POST request to /api/product-orders with JSON body
        // todo: Assert status 400 Bad Request
        fail("todo: Implement test");
    }

    // ==================== PUT /api/product-orders/{id} ====================

    @Test
    public void testUpdateProductOrder_Success() throws Exception {
        // todo: Create a fresh product order first (POST) to have a known id
        // todo: Build a valid ApiProductOrderDTO for update
        // todo: Send PUT request to /api/product-orders/{id} with JSON body
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateProductOrder_Failure_NotFound() throws Exception {
        String body = "{\"product_id\": 1, \"order_id\": 1, \"product_quantity\": 2, \"product_unit_cost\": 500}";

        mockMvc.perform(put("/api/product-orders/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/product-orders/{id} ====================

    @Test
    public void testDeleteProductOrder_Success() throws Exception {
        // todo: Create a fresh product order first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/product-orders/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteProductOrder_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/product-orders/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
