package com.rocketFoodDelivery.rocketFood.api.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.employee.ApiEmployeeDTO;
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
public class EmployeeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    // Helper: persist a fresh user (unique email) with no existing employee, return its id
    private int freshUserId() {
        User user = User.builder()
                .name("Test Employee User")
                .email("employee.test." + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        return userRepository.save(user).getId();
    }

    private int seededAddressId() {
        return addressRepository.findAll().get(0).getId();
    }

    // Helper: build a valid employee DTO referencing valid foreign keys
    private ApiEmployeeDTO buildEmployee(int userId, int addressId) {
        ApiEmployeeDTO dto = new ApiEmployeeDTO();
        dto.setUserId(userId);
        dto.setAddressId(addressId);
        dto.setPhone("+1-555-7890");
        dto.setEmail("employee@example.com");
        return dto;
    }

    // Helper: POST an employee (with a fresh user) and return its generated id
    private int createEmployeeAndGetId() throws Exception {
        ApiEmployeeDTO dto = buildEmployee(freshUserId(), seededAddressId());
        String response = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        int userId = freshUserId();
        int addressId = seededAddressId();
        ApiEmployeeDTO newEmployee = buildEmployee(userId, addressId);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.user_id").value(userId))
                .andExpect(jsonPath("$.data.address_id").value(addressId))
                .andExpect(jsonPath("$.data.phone").value("+1-555-7890"));
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
        int id = createEmployeeAndGetId();

        ApiEmployeeDTO update = new ApiEmployeeDTO();
        update.setPhone("+1-555-0000");
        update.setEmail("updated.employee@example.com");

        mockMvc.perform(put("/api/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.phone").value("+1-555-0000"))
                .andExpect(jsonPath("$.data.email").value("updated.employee@example.com"));
    }

    @Test
    public void testUpdateEmployee_Failure_NotFound() throws Exception {
        String body = "{\"user_id\": 1, \"address_id\": 1, \"phone\": \"+1-555-0000\", \"email\": \"test@test.com\"}";

        mockMvc.perform(put("/api/employees/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/employees/{id} ====================

    @Test
    public void testDeleteEmployee_Success() throws Exception {
        int id = createEmployeeAndGetId();

        mockMvc.perform(delete("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteEmployee_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/employees/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
