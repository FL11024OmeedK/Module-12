package com.rocketFoodDelivery.rocketFood.api.customer;

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
public class CustomerApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/customers ====================

    @Test
    public void testGetAllCustomers_Success() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/customers/{id} ====================

    @Test
    public void testGetCustomerById_Success() throws Exception {
        // todo: Send GET request to /api/customers/{id} with a known valid id (e.g. 1)
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response contains "data.id" matching the requested id
        fail("todo: Implement test");
    }

    @Test
    public void testGetCustomerById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/customers ====================

    @Test
    public void testCreateCustomer_Success() throws Exception {
        // todo: Create a fresh user first (POST /api/users) to get a valid user_id
        // todo: Build a valid ApiCustomerDTO (user_id, address_id, phone, email, active)
        // todo: Send POST request to /api/customers with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateCustomer_Failure_InvalidData() throws Exception {
        // todo: Send POST request to /api/customers with empty or invalid JSON body
        // todo: Assert status 400 Bad Request
        fail("todo: Implement test");
    }

    // ==================== PUT /api/customers/{id} ====================

    @Test
    public void testUpdateCustomer_Success() throws Exception {
        // todo: Build a valid ApiCustomerDTO for update
        // todo: Send PUT request to /api/customers/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateCustomer_Failure_NotFound() throws Exception {
        String body = "{\"user_id\": 1, \"address_id\": 1, \"phone\": \"+1-555-9999\", \"email\": \"test@test.com\", \"active\": true}";

        mockMvc.perform(put("/api/customers/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/customers/{id} ====================

    @Test
    public void testDeleteCustomer_Success() throws Exception {
        // todo: Create a fresh user and customer first (POST) to safely delete
        // todo: Extract the created customer id from the response
        // todo: Send DELETE request to /api/customers/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteCustomer_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/customers/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
