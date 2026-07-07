package com.rocketFoodDelivery.rocketFood.dtos.order;

// Jackson - JSON field mapping
import com.fasterxml.jackson.annotation.JsonProperty;

// Jakarta EE - Validation
import jakarta.validation.constraints.NotEmpty;

// Lombok - Code generation
import lombok.Getter;
import lombok.Setter;

// Java standard library
import java.util.List;

/**
 * Request DTO for creating an order.
 * Carries the restaurant, the customer, and the list of ordered products (id + quantity).
 */
@Getter
@Setter
public class ApiCreateOrderDTO {

    @JsonProperty("restaurant_id")
    private int restaurantId;

    @JsonProperty("customer_id")
    private int customerId;

    @NotEmpty(message = "Products are required")
    private List<ProductItem> products;

    /** A single ordered product: which product and how many. */
    @Getter
    @Setter
    public static class ProductItem {
        private int id;
        private int quantity;
    }
}
