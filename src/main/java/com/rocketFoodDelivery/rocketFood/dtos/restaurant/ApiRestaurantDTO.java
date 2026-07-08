package com.rocketFoodDelivery.rocketFood.dtos.restaurant;

// Jackson - JSON field mapping / conditional inclusion
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;

// Lombok - Code generation
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for the Restaurant table. Serves two shapes:
 *  - Summary (GET list / GET by id / DELETE): id, name, price_range, rating
 *  - Detailed (POST create / PUT update):     id, name, phone, email, user_id, price_range, address
 * @JsonInclude(NON_NULL) omits the fields not relevant to a given path.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRestaurantDTO {

    private int id;

    private String name;

    @JsonProperty("price_range")
    private int priceRange;

    // Summary shape only (set by mapRowToRestaurantDTO); omitted on create/update.
    private Integer rating;

    // Detailed shape only (create/update); omitted on GET/DELETE.
    private String phone;

    private String email;

    @JsonProperty("user_id")
    private Integer userId;

    private ApiAddressDTO address;
}
