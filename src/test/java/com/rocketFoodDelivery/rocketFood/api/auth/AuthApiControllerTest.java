package com.rocketFoodDelivery.rocketFood.api.auth;

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
public class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== POST /api/auth ====================

    @Test
    public void testAuthenticate_Success() throws Exception {
        String body = "{\"email\": \"both@gmail.com\", \"password\": \"password\"}";

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user_id").isNumber());
    }

    @Test
    public void testAuthenticate_Failure_BadCredentials() throws Exception {
        // todo: Build an AuthRequestDTO with a valid email but wrong password
        // todo: Send POST request to /api/auth with JSON body
        // todo: Assert status 401 Unauthorized
        // todo: Assert response contains "success": false
        fail("todo: Implement test");
    }

    @Test
    public void testAuthenticate_Failure_MissingFields() throws Exception {
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
