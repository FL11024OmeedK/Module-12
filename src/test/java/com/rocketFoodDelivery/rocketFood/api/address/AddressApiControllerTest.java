package com.rocketFoodDelivery.rocketFood.api.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional // roll back each test's DB writes so tests stay independent and non-destructive
public class AddressApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper: build a valid address DTO
    private ApiAddressDTO buildAddress(String street, String city, String postalCode) {
        ApiAddressDTO dto = new ApiAddressDTO();
        dto.setStreetAddress(street);
        dto.setCity(city);
        dto.setPostalCode(postalCode);
        return dto;
    }

    // Helper: POST an address and return its generated id
    private int createAddressAndGetId(ApiAddressDTO dto) throws Exception {
        String response = mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        ApiAddressDTO newAddress = buildAddress("789 Pine Blvd", "Toronto", "M5V2T6");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAddress)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.street_address").value("789 Pine Blvd"))
                .andExpect(jsonPath("$.data.city").value("Toronto"))
                .andExpect(jsonPath("$.data.postal_code").value("M5V2T6"));
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
        // Create a fresh address so the test does not depend on seeded data
        int id = createAddressAndGetId(buildAddress("111 Temp St", "Calgary", "T2P1A1"));

        ApiAddressDTO update = buildAddress("999 Updated Ave", "Vancouver", "V6B1A1");

        mockMvc.perform(put("/api/addresses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.street_address").value("999 Updated Ave"))
                .andExpect(jsonPath("$.data.city").value("Vancouver"))
                .andExpect(jsonPath("$.data.postal_code").value("V6B1A1"));
    }

    @Test
    public void testUpdateAddress_Failure_NotFound() throws Exception {
        ApiAddressDTO update = buildAddress("999 Updated Ave", "Vancouver", "V6B1A1");

        mockMvc.perform(put("/api/addresses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/addresses/{id} ====================

    @Test
    public void testDeleteAddress_Success() throws Exception {
        // Create a fresh address so we can safely delete it
        int id = createAddressAndGetId(buildAddress("222 Delete Me Rd", "Ottawa", "K1A0B1"));

        mockMvc.perform(delete("/api/addresses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteAddress_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/addresses/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
