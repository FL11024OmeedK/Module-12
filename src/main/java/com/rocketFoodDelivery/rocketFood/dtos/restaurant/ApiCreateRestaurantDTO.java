package com.rocketFoodDelivery.rocketFood.dtos.restaurant;

// Jackson - JSON field mapping
import com.fasterxml.jackson.annotation.JsonProperty;

// Jakarta EE - Validation
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;

// Lombok - Code generation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for creating and updating a restaurant.
 * On create, the nested address is required (validated in the service) and persisted.
 * On update, only name/price_range/phone are applied; address/user_id are ignored.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiCreateRestaurantDTO {

    @JsonProperty("user_id")
    private int userId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    @JsonProperty("price_range")
    @Min(value = 1, message = "Price range must be between 1 and 3")
    @Max(value = 3, message = "Price range must be between 1 and 3")
    private int priceRange;

    // Required on create (validated in the service), absent on update.
    @Valid
    private ApiAddressDTO address;
}
