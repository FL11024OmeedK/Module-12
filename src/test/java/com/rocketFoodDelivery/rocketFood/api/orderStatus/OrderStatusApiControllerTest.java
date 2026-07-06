package com.rocketFoodDelivery.rocketFood.api.orderStatus;

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
public class OrderStatusApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== POST /api/order/{order_id}/status (Custom Endpoint) ====================

    @Test
    public void testUpdateOrderStatus_Success() throws Exception {
        // todo: Build a valid ApiOrderStatusDTO with a valid status (e.g. "in progress")
        // todo: Send POST request to /api/order/{order_id}/status with JSON body and a known valid order id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data status matches the input
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateOrderStatus_Failure_InvalidStatus() throws Exception {
        // todo: Build an ApiOrderStatusDTO with an invalid status (e.g. "nonexistent_status")
        // todo: Send POST request to /api/order/{order_id}/status with JSON body
        // todo: Assert status 400 Bad Request
        fail("todo: Implement test");
    }

    // ==================== GET /api/order-statuses ====================

    @Test
    public void testGetAllOrderStatuses_Success() throws Exception {
        mockMvc.perform(get("/api/order-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/order-statuses/{id} ====================

    @Test
    public void testGetOrderStatusById_Success() throws Exception {
        mockMvc.perform(get("/api/order-statuses/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetOrderStatusById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/order-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/order-statuses ====================

    @Test
    public void testCreateOrderStatus_Success() throws Exception {
        // todo: Build a valid ApiOrderStatusCrudDTO (name)
        // todo: Send POST request to /api/order-statuses with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data name matches the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateOrderStatus_Failure_InvalidData() throws Exception {
        // todo: Send POST request to /api/order-statuses with empty or invalid JSON body
        // todo: Assert status 400 Bad Request
        fail("todo: Implement test");
    }

    // ==================== PUT /api/order-statuses/{id} ====================

    @Test
    public void testUpdateOrderStatusEntity_Success() throws Exception {
        // todo: Create a fresh order status first (POST) to have a known id
        // todo: Build a valid ApiOrderStatusCrudDTO for update
        // todo: Send PUT request to /api/order-statuses/{id} with JSON body
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateOrderStatusEntity_Failure_NotFound() throws Exception {
        String body = "{\"name\": \"ghost_status\"}";

        mockMvc.perform(put("/api/order-statuses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/order-statuses/{id} ====================

    @Test
    public void testDeleteOrderStatus_Success() throws Exception {
        // todo: Create a fresh order status first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/order-statuses/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteOrderStatus_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/order-statuses/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
