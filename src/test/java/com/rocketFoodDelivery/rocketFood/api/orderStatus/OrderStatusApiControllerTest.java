package com.rocketFoodDelivery.rocketFood.api.orderStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusCrudDTO;
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
public class OrderStatusApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Helper: build a valid CRUD DTO
    private ApiOrderStatusCrudDTO buildStatus(String name) {
        ApiOrderStatusCrudDTO dto = new ApiOrderStatusCrudDTO();
        dto.setName(name);
        return dto;
    }

    // Helper: POST an order status and return its generated id
    private int createStatusAndGetId(String name) throws Exception {
        String response = mockMvc.perform(post("/api/order-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildStatus(name))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    // ==================== POST /api/order/{order_id}/status (Custom Endpoint) ====================

    @Test
    public void testUpdateOrderStatus_Success() throws Exception {
        String body = "{\"status\": \"in progress\"}";

        mockMvc.perform(post("/api/order/{order_id}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.status").value("in progress"));
    }

    @Test
    public void testUpdateOrderStatus_Failure_InvalidStatus() throws Exception {
        String body = "{\"status\": \"nonexistent_status\"}";

        mockMvc.perform(post("/api/order/{order_id}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ==================== GET /api/order-statuses ====================

    @Test
    public void testGetAllOrderStatuses_Success() throws Exception {
        mockMvc.perform(get("/api/order-statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/order-statuses/{id} ====================

    @Test
    public void testGetOrderStatusById_Success() throws Exception {
        mockMvc.perform(get("/api/order-statuses/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetOrderStatusById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/order-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/order-statuses ====================

    @Test
    public void testCreateOrderStatus_Success() throws Exception {
        ApiOrderStatusCrudDTO newStatus = buildStatus("archived");

        mockMvc.perform(post("/api/order-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStatus)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("archived"));
    }

    @Test
    public void testCreateOrderStatus_Failure_InvalidData() throws Exception {
        mockMvc.perform(post("/api/order-statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/order-statuses/{id} ====================

    @Test
    public void testUpdateOrderStatusEntity_Success() throws Exception {
        int id = createStatusAndGetId("temporary");

        ApiOrderStatusCrudDTO update = buildStatus("renamed_status");

        mockMvc.perform(put("/api/order-statuses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("renamed_status"));
    }

    @Test
    public void testUpdateOrderStatusEntity_Failure_NotFound() throws Exception {
        String body = "{\"name\": \"ghost_status\"}";

        mockMvc.perform(put("/api/order-statuses/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/order-statuses/{id} ====================

    @Test
    public void testDeleteOrderStatus_Success() throws Exception {
        int id = createStatusAndGetId("to_delete");

        mockMvc.perform(delete("/api/order-statuses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteOrderStatus_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/order-statuses/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
