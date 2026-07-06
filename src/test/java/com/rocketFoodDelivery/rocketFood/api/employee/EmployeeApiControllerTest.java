package com.rocketFoodDelivery.rocketFood.api.employee;

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
public class EmployeeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/employees ====================

    @Test
    public void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/employees/{id} ====================

    @Test
    public void testGetEmployeeById_Success() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetEmployeeById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/employees/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/employees ====================

    @Test
    public void testCreateEmployee_Success() throws Exception {
        // todo: Create a fresh user first (POST /api/users) to get a valid user_id
        // todo: Build a valid ApiEmployeeDTO (user_id, address_id, phone, email)
        // todo: Send POST request to /api/employees with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateEmployee_Failure_InvalidData() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/employees/{id} ====================

    @Test
    public void testUpdateEmployee_Success() throws Exception {
        // todo: Build a valid ApiEmployeeDTO for update
        // todo: Send PUT request to /api/employees/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateEmployee_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiEmployeeDTO for update
        // todo: Send PUT request to /api/employees/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/employees/{id} ====================

    @Test
    public void testDeleteEmployee_Success() throws Exception {
        // todo: Create a fresh user and employee first (POST) to safely delete
        // todo: Extract the created employee id from the response
        // todo: Send DELETE request to /api/employees/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteEmployee_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
