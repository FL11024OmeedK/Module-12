package com.rocketFoodDelivery.rocketFood.api.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiCreateOrderDTO;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.repository.CourierRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional // roll back each test's DB writes so tests stay independent and non-destructive
public class OrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CourierRepository courierRepository;

    // Helper: build a valid create-order DTO from seeded data (a product + its restaurant + a customer)
    private ApiCreateOrderDTO buildValidOrder() {
        Product product = productRepository.findAll().get(0);
        int restaurantId = product.getRestaurant().getId();
        int customerId = customerRepository.findAll().get(0).getId();

        ApiCreateOrderDTO dto = new ApiCreateOrderDTO();
        dto.setRestaurantId(restaurantId);
        dto.setCustomerId(customerId);
        ApiCreateOrderDTO.ProductItem item = new ApiCreateOrderDTO.ProductItem();
        item.setId(product.getId());
        item.setQuantity(2);
        dto.setProducts(List.of(item));
        return dto;
    }

    // Helper: POST an order and return its generated id
    private int createOrderAndGetId() throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidOrder())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    // ==================== GET /api/orders ====================

    @Test
    public void testGetOrders_Success() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("type", "customer")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    public void testGetOrders_Failure_InvalidType() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .param("type", "invalid")
                        .param("id", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==================== POST /api/orders ====================

    @Test
    public void testCreateOrder_Success() throws Exception {
        ApiCreateOrderDTO dto = buildValidOrder();

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.customer_id").value(dto.getCustomerId()))
                .andExpect(jsonPath("$.data.restaurant_id").value(dto.getRestaurantId()))
                .andExpect(jsonPath("$.data.status").value("pending"))
                .andExpect(jsonPath("$.data.products").isArray())
                .andExpect(jsonPath("$.data.total_cost").isNumber());
    }

    @Test
    public void testCreateOrder_Failure_InvalidData() throws Exception {
        // Valid products list but a non-existent restaurant -> service throws BadRequestException
        ApiCreateOrderDTO dto = buildValidOrder();
        dto.setRestaurantId(999999);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==================== PUT /api/orders/{id} ====================

    @Test
    public void testUpdateOrder_Success() throws Exception {
        int orderId = createOrderAndGetId();

        int customerId = customerRepository.findAll().get(0).getId();
        int restaurantId = productRepository.findAll().get(0).getRestaurant().getId();
        int courierId = courierRepository.findAll().get(0).getId();

        String body = String.format(
                "{\"customer_id\": %d, \"restaurant_id\": %d, \"courier_id\": %d}",
                customerId, restaurantId, courierId);

        mockMvc.perform(put("/api/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    public void testUpdateOrder_Failure_NotFound() throws Exception {
        String body = "{\"customer_id\": 1, \"restaurant_id\": 1, \"courier_id\": 1}";

        mockMvc.perform(put("/api/orders/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/orders/{id} ====================

    @Test
    public void testDeleteOrder_Success() throws Exception {
        int orderId = createOrderAndGetId();

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(orderId));
    }

    @Test
    public void testDeleteOrder_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/orders/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
