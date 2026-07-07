package com.rocketFoodDelivery.rocketFood.api.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiCreateProductDTO;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
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
public class ProductApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private int seededRestaurantId() {
        return restaurantRepository.findAll().get(0).getId();
    }

    // Helper: build a valid product DTO referencing a seeded restaurant
    private ApiCreateProductDTO buildProduct(String name, int cost) {
        ApiCreateProductDTO dto = new ApiCreateProductDTO();
        dto.setRestaurantId(seededRestaurantId());
        dto.setName(name);
        dto.setDescription("A tasty test item");
        dto.setCost(cost);
        return dto;
    }

    // Helper: POST a product and return its generated id
    private int createProductAndGetId() throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProduct("Test Product", 500))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    // ==================== GET /api/products ====================

    @Test
    public void testGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/products/{id} ====================

    @Test
    public void testGetProductById_Success() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetProductById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/products ====================

    @Test
    public void testCreateProduct_Success() throws Exception {
        ApiCreateProductDTO newProduct = buildProduct("New Burger", 899);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.restaurant_id").value(newProduct.getRestaurantId()))
                .andExpect(jsonPath("$.data.name").value("New Burger"))
                .andExpect(jsonPath("$.data.cost").value(899));
    }

    @Test
    public void testCreateProduct_Failure_MissingName() throws Exception {
        String body = "{\"cost\": 10, \"restaurant_id\": 1}";

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/products/{id} ====================

    @Test
    public void testUpdateProduct_Success() throws Exception {
        int id = createProductAndGetId();

        ApiCreateProductDTO update = buildProduct("Updated Burger", 999);
        update.setDescription("An updated description");

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.name").value("Updated Burger"))
                .andExpect(jsonPath("$.data.cost").value(999));
    }

    @Test
    public void testUpdateProduct_Failure_NotFound() throws Exception {
        ApiCreateProductDTO update = buildProduct("Ghost Product", 100);

        mockMvc.perform(put("/api/products/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/products/{id} ====================

    @Test
    public void testDeleteProduct_Success() throws Exception {
        int id = createProductAndGetId();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteProduct_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
