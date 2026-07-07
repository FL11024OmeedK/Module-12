package com.rocketFoodDelivery.rocketFood.dtos.product;

// Jackson - JSON field mapping
import com.fasterxml.jackson.annotation.JsonProperty;

// Lombok - Code generation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for the Product table.
 * Internal timestamps are omitted; the restaurant is referenced by id.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiProductDTO {

    private int id;

    @JsonProperty("restaurant_id")
    private int restaurantId;

    private String name;

    private String description;

    private int cost;
}
