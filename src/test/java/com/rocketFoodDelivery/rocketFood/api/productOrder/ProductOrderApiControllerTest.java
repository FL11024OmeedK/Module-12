package com.rocketFoodDelivery.rocketFood.api.productOrder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rocketFoodDelivery.rocketFood.dtos.productOrder.ApiProductOrderDTO;
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.Order;
import com.rocketFoodDelivery.rocketFood.models.OrderStatus;
import com.rocketFoodDelivery.rocketFood.models.Product;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderRepository;
import com.rocketFoodDelivery.rocketFood.repository.OrderStatusRepository;
import com.rocketFoodDelivery.rocketFood.repository.ProductRepository;
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
public class ProductOrderApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Helper: persist a fresh product and a fresh order for the SAME restaurant.
    // Returns [productId, orderId]. The fresh order has no product_orders yet, so the
    // duplicate-product rule cannot trip, and both belong to the same restaurant.
    private int[] freshProductAndOrder() {
        Restaurant restaurant = restaurantRepository.findAll().get(0);
        Customer customer = customerRepository.findAll().get(0);
        OrderStatus status = orderStatusRepository.findAll().get(0);

        Product product = productRepository.save(Product.builder()
                .restaurant(restaurant)
                .name("PO Test Product")
                .description("test item")
                .cost(500)
                .build());

        Order order = orderRepository.save(Order.builder()
                .restaurant(restaurant)
                .customer(customer)
                .orderStatus(status)
                .build());

        return new int[]{product.getId(), order.getId()};
    }

    private ApiProductOrderDTO buildProductOrder(int productId, int orderId, int quantity, int unitCost) {
        ApiProductOrderDTO dto = new ApiProductOrderDTO();
        dto.setProductId(productId);
        dto.setOrderId(orderId);
        dto.setProductQuantity(quantity);
        dto.setProductUnitCost(unitCost);
        return dto;
    }

    // Helper: create a product order (for a fresh product/order pair) and return its generated id
    private int createProductOrderAndGetId() throws Exception {
        int[] ids = freshProductAndOrder();
        String response = mockMvc.perform(post("/api/product-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProductOrder(ids[0], ids[1], 2, 500))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    // ==================== GET /api/product-orders ====================

    @Test
    public void testGetAllProductOrders_Success() throws Exception {
        mockMvc.perform(get("/api/product-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== GET /api/product-orders/{id} ====================

    @Test
    public void testGetProductOrderById_Success() throws Exception {
        mockMvc.perform(get("/api/product-orders/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetProductOrderById_Failure_NotFound() throws Exception {
        mockMvc.perform(get("/api/product-orders/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /api/product-orders ====================

    @Test
    public void testCreateProductOrder_Success() throws Exception {
        int[] ids = freshProductAndOrder();
        ApiProductOrderDTO newProductOrder = buildProductOrder(ids[0], ids[1], 2, 1975);

        mockMvc.perform(post("/api/product-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProductOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.product_id").value(ids[0]))
                .andExpect(jsonPath("$.data.order_id").value(ids[1]))
                .andExpect(jsonPath("$.data.product_quantity").value(2))
                .andExpect(jsonPath("$.data.product_unit_cost").value(1975));
    }

    @Test
    public void testCreateProductOrder_Failure_InvalidData() throws Exception {
        // Non-existent product and order ids -> business rule (same restaurant) fails -> 400
        ApiProductOrderDTO invalid = buildProductOrder(999999, 999999, 2, 500);

        mockMvc.perform(post("/api/product-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PUT /api/product-orders/{id} ====================

    @Test
    public void testUpdateProductOrder_Success() throws Exception {
        int[] ids = freshProductAndOrder();
        String createResponse = mockMvc.perform(post("/api/product-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildProductOrder(ids[0], ids[1], 2, 500))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = objectMapper.readTree(createResponse).path("data").path("id").asInt();

        ApiProductOrderDTO update = buildProductOrder(ids[0], ids[1], 5, 750);

        mockMvc.perform(put("/api/product-orders/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.product_quantity").value(5))
                .andExpect(jsonPath("$.data.product_unit_cost").value(750));
    }

    @Test
    public void testUpdateProductOrder_Failure_NotFound() throws Exception {
        String body = "{\"product_id\": 1, \"order_id\": 1, \"product_quantity\": 2, \"product_unit_cost\": 500}";

        mockMvc.perform(put("/api/product-orders/{id}", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/product-orders/{id} ====================

    @Test
    public void testDeleteProductOrder_Success() throws Exception {
        int id = createProductOrderAndGetId();

        mockMvc.perform(delete("/api/product-orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    public void testDeleteProductOrder_Failure_NotFound() throws Exception {
        mockMvc.perform(delete("/api/product-orders/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
