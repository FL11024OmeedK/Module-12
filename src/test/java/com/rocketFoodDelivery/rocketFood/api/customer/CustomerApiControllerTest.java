package com.rocketFoodDelivery.rocketFood.api.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.customer.ApiCustomerDTO;
import com.rocketFoodDelivery.rocketFood.models.User;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
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
public class CustomerApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    // Helper: persist a fresh user (unique email) with no existing customer, return its id
    private int freshUserId() {
        User user = User.builder()
                .name("Test Customer User")
                .email("customer.test." + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        return userRepository.save(user).getId();
    }

    private int seededAddressId() {
        return addressRepository.findAll().get(0).getId();
    }

    // Helper: build a valid customer DTO referencing valid foreign keys
    private ApiCustomerDTO buildCustomer(int userId, int addressId) {
        ApiCustomerDTO dto = new ApiCustomerDTO();
        dto.setUserId(userId);
        dto.setAddressId(addressId);
        dto.setPhone("+1-555-1234");
        dto.setEmail("customer@example.com");
        dto.setActive(true);
        return dto;
    }

    // Helper: POST a customer (with a fresh user) and return its generated id
    private int createCustomerAndGetId() throws Exception {
        ApiCustomerDTO dto = buildCustomer(freshUserId(), seededAddressId());
        String response = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        int id = createCustomerAndGetId();

        mockMvc.perform(get("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testGetCustomerById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/customers ====================

    @Test
    public void testCreateCustomer_Success() throws Exception {
        int userId = freshUserId();
        int addressId = seededAddressId();
        ApiCustomerDTO newCustomer = buildCustomer(userId, addressId);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.user_id").value(userId))
                .andExpect(jsonPath("$.data.address_id").value(addressId))
                .andExpect(jsonPath("$.data.phone").value("+1-555-1234"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    public void testCreateCustomer_Failure_InvalidData() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/customers/{id} ====================

    @Test
    public void testUpdateCustomer_Success() throws Exception {
        int id = createCustomerAndGetId();

        ApiCustomerDTO update = new ApiCustomerDTO();
        update.setPhone("+1-555-9999");
        update.setEmail("updated@example.com");
        update.setActive(false);

        mockMvc.perform(put("/api/customers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.phone").value("+1-555-9999"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.active").value(false));
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
        int id = createCustomerAndGetId();

        mockMvc.perform(delete("/api/customers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteCustomer_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/customers/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
