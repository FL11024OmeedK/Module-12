package com.rocketFoodDelivery.rocketFood.api.order;

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
public class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/orders ====================

    @Test
    public void testGetOrders_Success() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("type", "customer")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetOrders_Failure_InvalidType() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("type", "invalid")
                        .param("id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==================== POST /api/orders ====================

    @Test
    public void testCreateOrder_Success() throws Exception {
        // todo: Build a valid ApiCreateOrderDTO (restaurantId, customerId, products list)
        // todo: Send POST request to /api/orders with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response data contains id, customer_id, restaurant_id, status="pending", products array, total_cost
        fail("todo: Implement test");
    }

    @Test
    public void testCreateOrder_Failure_InvalidData() throws Exception {
        // todo: Build an invalid ApiCreateOrderDTO (non-existent restaurant/customer, empty products)
        // todo: Send POST request to /api/orders with JSON body
        // todo: Assert status 400 Bad Request
        // todo: Assert response contains "error": "Bad Request"
        fail("todo: Implement test");
    }

    // ==================== PUT /api/orders/{id} ====================

    @Test
    public void testUpdateOrder_Success() throws Exception {
        // todo: Build a valid ApiUpdateOrderDTO (customer_id, restaurant_id, courier_id)
        // todo: Send PUT request to /api/orders/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data id matches the requested id
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateOrder_Failure_NotFound() throws Exception {
        String body = "{\"customer_id\": 1, \"restaurant_id\": 1, \"courier_id\": 1}";

        mockMvc.perform(put("/api/orders/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/orders/{id} ====================

    @Test
    public void testDeleteOrder_Success() throws Exception {
        // todo: Create a fresh order first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/orders/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteOrder_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/orders/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
