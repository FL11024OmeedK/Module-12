package com.rocketFoodDelivery.rocketFood.api.courier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.courier.ApiCourierDTO;
import com.rocketFoodDelivery.rocketFood.models.User;
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.CourierStatusRepository;
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
public class CourierApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CourierStatusRepository courierStatusRepository;

    // Helper: persist a fresh user (unique email) with no existing courier, return its id
    private int freshUserId() {
        User user = User.builder()
                .name("Test Courier User")
                .email("courier.test." + UUID.randomUUID() + "@example.com")
                .password("password")
                .build();
        return userRepository.save(user).getId();
    }

    private int seededAddressId() {
        return addressRepository.findAll().get(0).getId();
    }

    private int seededCourierStatusId() {
        return courierStatusRepository.findAll().get(0).getId();
    }

    // Helper: build a valid courier DTO referencing valid foreign keys
    private ApiCourierDTO buildCourier(int userId, int addressId, int courierStatusId) {
        ApiCourierDTO dto = new ApiCourierDTO();
        dto.setUserId(userId);
        dto.setAddressId(addressId);
        dto.setCourierStatusId(courierStatusId);
        dto.setPhone("+1-555-4567");
        dto.setEmail("courier@example.com");
        dto.setActive(true);
        return dto;
    }

    // Helper: POST a courier (with a fresh user) and return its generated id
    private int createCourierAndGetId() throws Exception {
        ApiCourierDTO dto = buildCourier(freshUserId(), seededAddressId(), seededCourierStatusId());
        String response = mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        int id = createCourierAndGetId();

        mockMvc.perform(get("/api/couriers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testGetCourierById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/couriers/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/couriers ====================

    @Test
    public void testCreateCourier_Success() throws Exception {
        int userId = freshUserId();
        int addressId = seededAddressId();
        int courierStatusId = seededCourierStatusId();
        ApiCourierDTO newCourier = buildCourier(userId, addressId, courierStatusId);

        mockMvc.perform(post("/api/couriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCourier)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.user_id").value(userId))
                .andExpect(jsonPath("$.data.address_id").value(addressId))
                .andExpect(jsonPath("$.data.courier_status_id").value(courierStatusId))
                .andExpect(jsonPath("$.data.phone").value("+1-555-4567"))
                .andExpect(jsonPath("$.data.active").value(true));
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
        int id = createCourierAndGetId();

        ApiCourierDTO update = new ApiCourierDTO();
        update.setCourierStatusId(seededCourierStatusId());
        update.setPhone("+1-555-0000");
        update.setEmail("updated.courier@example.com");
        update.setActive(false);

        mockMvc.perform(put("/api/couriers/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.phone").value("+1-555-0000"))
                .andExpect(jsonPath("$.data.email").value("updated.courier@example.com"))
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    public void testUpdateCourier_Failure_NotFound() throws Exception {
        ApiCourierDTO update = new ApiCourierDTO();
        update.setCourierStatusId(seededCourierStatusId());
        update.setPhone("+1-555-0000");
        update.setEmail("updated.courier@example.com");
        update.setActive(true);

        mockMvc.perform(put("/api/couriers/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/couriers/{id} ====================

    @Test
    public void testDeleteCourier_Success() throws Exception {
        int id = createCourierAndGetId();

        mockMvc.perform(delete("/api/couriers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteCourier_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/couriers/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
