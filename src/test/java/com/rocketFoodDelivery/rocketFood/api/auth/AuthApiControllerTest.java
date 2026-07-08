package com.rocketFoodDelivery.rocketFood.api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
        String body = "{\"email\": \"both@gmail.com\", \"password\": \"wrong-password\"}";

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testAuthenticate_Failure_MissingFields() throws Exception {
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
