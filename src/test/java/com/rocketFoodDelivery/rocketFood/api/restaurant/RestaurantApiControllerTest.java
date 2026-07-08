package com.rocketFoodDelivery.rocketFood.api.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiCreateRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;
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
public class RestaurantApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private int seededUserId() {
        return userRepository.findAll().get(0).getId();
    }

    // Helper: build a valid create DTO with a nested address, referencing a seeded user
    private ApiCreateRestaurantDTO buildRestaurant(String name, int priceRange) {
        ApiAddressDTO address = new ApiAddressDTO();
        address.setStreetAddress("123 Wellington St.");
        address.setCity("Montreal");
        address.setPostalCode("H3G264");

        ApiCreateRestaurantDTO dto = new ApiCreateRestaurantDTO();
        dto.setUserId(seededUserId());
        dto.setName(name);
        dto.setPhone("15141234567");
        dto.setEmail("villa@wellington.com");
        dto.setPriceRange(priceRange);
        dto.setAddress(address);
        return dto;
    }

    // Helper: POST a restaurant and return its generated id
    private int createRestaurantAndGetId() throws Exception {
        String response = mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRestaurant("Test Restaurant", 2))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    // ==================== GET /api/restaurants ====================

    @Test
    public void testGetAllRestaurants_Success() throws Exception {
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/restaurants/{id} ====================

    @Test
    public void testGetRestaurantById_Success() throws Exception {
        mockMvc.perform(get("/api/restaurants/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.rating").exists());
    }

    @Test
    public void testGetRestaurantById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/restaurants/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/restaurants ====================

    @Test
    public void testCreateRestaurant_Success() throws Exception {
        ApiCreateRestaurantDTO newRestaurant = buildRestaurant("Villa Wellington", 2);

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRestaurant)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Villa Wellington"))
                .andExpect(jsonPath("$.data.price_range").value(2))
                .andExpect(jsonPath("$.data.user_id").value(newRestaurant.getUserId()))
                .andExpect(jsonPath("$.data.phone").value("15141234567"))
                .andExpect(jsonPath("$.data.address.street_address").value("123 Wellington St."))
                .andExpect(jsonPath("$.data.address.id").isNumber());
    }

    @Test
    public void testCreateRestaurant_Failure_MissingAddress() throws Exception {
        // Valid required fields but no address -> service rejects with 400
        String body = String.format(
                "{\"user_id\": %d, \"name\": \"No Address\", \"phone\": \"5145550000\", \"price_range\": 2}",
                seededUserId());

        mockMvc.perform(post("/api/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/restaurants/{id} ====================

    @Test
    public void testUpdateRestaurant_Success() throws Exception {
        // Update a seeded restaurant (rolled back afterwards)
        String body = "{\"name\": \"B12 Nation\", \"price_range\": 3, \"phone\": \"2223334444\"}";

        mockMvc.perform(put("/api/restaurants/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("B12 Nation"))
                .andExpect(jsonPath("$.data.price_range").value(3))
                .andExpect(jsonPath("$.data.phone").value("2223334444"));
    }

    @Test
    public void testUpdateRestaurant_Failure_NotFound() throws Exception {
        String body = "{\"name\": \"Ghost\", \"price_range\": 2, \"phone\": \"0000000000\"}";

        mockMvc.perform(put("/api/restaurants/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/restaurants/{id} ====================

    @Test
    public void testDeleteRestaurant_Success() throws Exception {
        // Create a fresh restaurant (no products/orders) so it can be deleted cleanly
        int id = createRestaurantAndGetId();

        mockMvc.perform(delete("/api/restaurants/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteRestaurant_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/restaurants/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
