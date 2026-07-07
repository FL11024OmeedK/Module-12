package com.rocketFoodDelivery.rocketFood.dtos.product;

// Jackson - JSON field mapping
import com.fasterxml.jackson.annotation.JsonProperty;

// Jakarta EE - Validation
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Lombok - Code generation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating and updating a product.
 * On update, restaurant_id is ignored (a product's restaurant is immutable).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiCreateProductDTO {

    @JsonProperty("restaurant_id")
    private int restaurantId;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Cost is required")
    @Min(value = 0, message = "Cost must be greater than or equal to 0")
    private Integer cost;
}
