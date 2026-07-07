package com.rocketFoodDelivery.rocketFood.api.courierStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.courierStatus.ApiCourierStatusDTO;
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
public class CourierStatusApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper: build a valid courier status DTO
    private ApiCourierStatusDTO buildStatus(String name) {
        ApiCourierStatusDTO dto = new ApiCourierStatusDTO();
        dto.setName(name);
        return dto;
    }

    // Helper: POST a courier status and return its generated id
    private int createStatusAndGetId(String name) throws Exception {
        String response = mockMvc.perform(post("/api/courier-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildStatus(name))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

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
        mockMvc.perform(get("/api/courier-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/courier-statuses ====================

    @Test
    public void testCreateCourierStatus_Success() throws Exception {
        ApiCourierStatusDTO newStatus = buildStatus("on_break");

        mockMvc.perform(post("/api/courier-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("on_break"));
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
        int id = createStatusAndGetId("temporary");

        ApiCourierStatusDTO update = buildStatus("updated_status");

        mockMvc.perform(put("/api/courier-statuses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("updated_status"));
    }

    @Test
    public void testUpdateCourierStatus_Failure_NotFound() throws Exception {
        ApiCourierStatusDTO update = buildStatus("updated_status");

        mockMvc.perform(put("/api/courier-statuses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/courier-statuses/{id} ====================

    @Test
    public void testDeleteCourierStatus_Success() throws Exception {
        int id = createStatusAndGetId("to_delete");

        mockMvc.perform(delete("/api/courier-statuses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteCourierStatus_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/courier-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
