package com.rocketFoodDelivery.rocketFood.api.address;

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
public class AddressApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/addresses ====================

    @Test
    public void testGetAllAddresses_Success() throws Exception {
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/addresses/{id} ====================

    @Test
    public void testGetAddressById_Success() throws Exception {
        mockMvc.perform(get("/api/addresses/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetAddressById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/addresses/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/addresses ====================

    @Test
    public void testCreateAddress_Success() throws Exception {
        // todo: Build a valid ApiAddressDTO (street_address, city, postal_code)
        // todo: Send POST request to /api/addresses with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateAddress_Failure_MissingFields() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/addresses/{id} ====================

    @Test
    public void testUpdateAddress_Success() throws Exception {
        // todo: Build a valid ApiAddressDTO for update
        // todo: Send PUT request to /api/addresses/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateAddress_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiAddressDTO for update
        // todo: Send PUT request to /api/addresses/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/addresses/{id} ====================

    @Test
    public void testDeleteAddress_Success() throws Exception {
        // todo: Create a fresh address first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/addresses/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteAddress_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/addresses/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
