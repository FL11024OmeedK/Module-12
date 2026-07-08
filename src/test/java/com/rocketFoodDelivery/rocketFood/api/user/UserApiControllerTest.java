package com.rocketFoodDelivery.rocketFood.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.user.ApiCreateUserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional // roll back each test's DB writes so tests stay independent and non-destructive
public class UserApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper: build a valid create/update DTO with a unique email (email is unique)
    private ApiCreateUserDTO buildUser(String name) {
        ApiCreateUserDTO dto = new ApiCreateUserDTO();
        dto.setName(name);
        dto.setEmail("user.test." + UUID.randomUUID() + "@example.com");
        dto.setPassword("password");
        return dto;
    }

    // Helper: POST a user and return its generated id
    private int createUserAndGetId() throws Exception {
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUser("Test User"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        mockMvc.perform(get("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/users ====================

    @Test
    public void testCreateUser_Success() throws Exception {
        ApiCreateUserDTO newUser = buildUser("Jane Smith");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Jane Smith"))
                .andExpect(jsonPath("$.data.email").value(newUser.getEmail()))
                // password must never be returned
                .andExpect(jsonPath("$.data.password").doesNotExist());
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
        int id = createUserAndGetId();

        ApiCreateUserDTO update = buildUser("Jane Updated");

        mockMvc.perform(put("/api/users/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("Jane Updated"))
                .andExpect(jsonPath("$.data.email").value(update.getEmail()));
    }

    @Test
    public void testUpdateUser_Failure_NotFound() throws Exception {
        ApiCreateUserDTO update = buildUser("Ghost User");

        mockMvc.perform(put("/api/users/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/users/{id} ====================

    @Test
    public void testDeleteUser_Success() throws Exception {
        int id = createUserAndGetId();

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteUser_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
