package com.rocketFoodDelivery.rocketFood.api.courierStatus;

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
public class CourierStatusApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/courier-statuses ====================

    @Test
    public void testGetAllCourierStatuses_Success() throws Exception {
        mockMvc.perform(get("/api/courier-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/courier-statuses/{id} ====================

    @Test
    public void testGetCourierStatusById_Success() throws Exception {
        mockMvc.perform(get("/api/courier-statuses/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetCourierStatusById_Failure_NotFound() throws Exception {
        // todo: Send GET request to /api/courier-statuses/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== POST /api/courier-statuses ====================

    @Test
    public void testCreateCourierStatus_Success() throws Exception {
        // todo: Build a valid ApiCourierStatusDTO (name)
        // todo: Send POST request to /api/courier-statuses with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data name matches the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateCourierStatus_Failure_InvalidData() throws Exception {
        mockMvc.perform(post("/api/courier-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/courier-statuses/{id} ====================

    @Test
    public void testUpdateCourierStatus_Success() throws Exception {
        // todo: Build a valid ApiCourierStatusDTO for update
        // todo: Send PUT request to /api/courier-statuses/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateCourierStatus_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiCourierStatusDTO for update
        // todo: Send PUT request to /api/courier-statuses/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/courier-statuses/{id} ====================

    @Test
    public void testDeleteCourierStatus_Success() throws Exception {
        // todo: Create a fresh courier status first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/courier-statuses/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteCourierStatus_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/courier-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
