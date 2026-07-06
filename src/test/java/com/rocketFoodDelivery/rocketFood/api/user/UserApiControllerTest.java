package com.rocketFoodDelivery.rocketFood.api.user;

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
public class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== GET /api/users ====================

    @Test
    public void testGetAllUsers_Success() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/users/{id} ====================

    @Test
    public void testGetUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetUserById_Failure_NotFound() throws Exception {
        // todo: Send GET request to /api/users/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== POST /api/users ====================

    @Test
    public void testCreateUser_Success() throws Exception {
        // todo: Build a valid ApiCreateUserDTO (name, email, password) with a unique email
        // todo: Send POST request to /api/users with JSON body
        // todo: Assert status 201 Created
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data name and email match the input
        fail("todo: Implement test");
    }

    @Test
    public void testCreateUser_Failure_MissingFields() throws Exception {
        String body = "{\"name\": \"Test User\"}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/users/{id} ====================

    @Test
    public void testUpdateUser_Success() throws Exception {
        // todo: Build a valid ApiCreateUserDTO for update
        // todo: Send PUT request to /api/users/{id} with JSON body and a known valid id
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        // todo: Assert response data fields reflect the update
        fail("todo: Implement test");
    }

    @Test
    public void testUpdateUser_Failure_NotFound() throws Exception {
        // todo: Build a valid ApiCreateUserDTO for update
        // todo: Send PUT request to /api/users/{id} with a non-existent id (e.g. 999999)
        // todo: Assert status 404 Not Found
        fail("todo: Implement test");
    }

    // ==================== DELETE /api/users/{id} ====================

    @Test
    public void testDeleteUser_Success() throws Exception {
        // todo: Create a fresh user first (POST) to safely delete
        // todo: Extract the created id from the response
        // todo: Send DELETE request to /api/users/{id}
        // todo: Assert status 200 OK
        // todo: Assert response contains "message": "Success"
        fail("todo: Implement test");
    }

    @Test
    public void testDeleteUser_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
