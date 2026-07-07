package com.rocketFoodDelivery.rocketFood.dtos.order;

// Jackson - JSON field mapping
import com.fasterxml.jackson.annotation.JsonProperty;

// Lombok - Code generation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for updating an order: reassigns customer, restaurant, and (optionally) courier.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiUpdateOrderDTO {

    @JsonProperty("customer_id")
    private int customerId;

    @JsonProperty("restaurant_id")
    private int restaurantId;

    @JsonProperty("courier_id")
    private Integer courierId;
}
