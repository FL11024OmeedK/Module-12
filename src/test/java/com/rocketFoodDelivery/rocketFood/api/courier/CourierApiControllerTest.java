package com.rocketFoodDelivery.rocketFood.api.courier;

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
public class CourierApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/couriers ====================

    @Test
    public void testGetAllCouriers_Success() throws Exception {
        mockMvc.perform(get("/api/couriers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/couriers/{id} ====================

    @Test
    public void testGetCourierById_Success() throws Exception {
        // todo: Send GET request to /api/couriers/{id} with a known valid id (e.g. 1)
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response contains "data.id" matching the requested id
        fail("todo: Implement test");
    }

    @Test
    public void testGetCourierById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/couriers/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/couriers ====================

    @Test
    public void testCreateCourier_Success() throws Exception {
        // todo: Create a fresh user first (POST /api/users) to get a valid user_id
        // todo: Build a valid ApiCourierDTO (user_id, courier_status_id, address_id, phone, email, active)
        // todo: Send POST request to /api/couriers with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateCourier_Failure_InvalidData() throws Exception {
        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/couriers/{id} ====================

    @Test
    public void testUpdateCourier_Success() throws Exception {
        // todo: Create a fresh courier first (POST) to have a known id to update
        // todo: Build a valid ApiCourierDTO for update
        // todo: Send PUT request to /api/couriers/{id} with JSON body
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateCourier_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiCourierDTO for update
        // todo: Send PUT request to /api/couriers/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/couriers/{id} ====================

    @Test
    public void testDeleteCourier_Success() throws Exception {
        // todo: Create a fresh user and courier first (POST) to safely delete
        // todo: Extract the created courier id from the response
        // todo: Send DELETE request to /api/couriers/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteCourier_Failure_NotFound() throws Exception {
        // todo: Send DELETE request to /api/couriers/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }
}
