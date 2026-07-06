package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;
import com.rocketFoodDelivery.rocketFood.models.User;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiCreateRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;

@Service       
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressRepository addressRepository;

    // Constructor
    public RestaurantService(RestaurantRepository restaurantRepository, AddressRepository addressRepository){
        this.restaurantRepository = restaurantRepository;
        this.addressRepository = addressRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Restaurant saveRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    // READ - Find all restaurants using JPA
    public List<Restaurant> findAllRestaurants() {
        return restaurantRepository.findAll();
    }

    // READ - Find a restaurant by ID using JPA
    public Optional<Restaurant> findRestaurantById(int id) {
        return restaurantRepository.findById(id);
    }

    // READ - Find a restaurant with average rating by ID using native SQL
    public List<Object[]> findRestaurantWithAverageRatingById(int restaurantId) {
        return restaurantRepository.findRestaurantWithAverageRatingById(restaurantId);
    }

    // READ - Find restaurants by rating and price range using native SQL
    public List<Object[]> findRestaurantsByRatingAndPriceRange(Integer rating, Integer priceRange) {
        return restaurantRepository.findRestaurantsByRatingAndPriceRange(rating, priceRange);
    }

    // DELETE - Delete a restaurant by ID using JPA
    public void deleteRestaurantById(int id) {
        restaurantRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================
    // todo: Implement service methods that use DTOs for input/output.


    // CREATE - Create an address from DTO
    // todo: Implement service method to create an address from DTO, return created DTO with ID
    

    // READ - Get all addresses as DTOs
    // todo: Implement service method to get all addresses as DTOs


    // READ - Get an address by ID as DTO
    // todo: Implement service method to get an address by ID as DTO, return Optional.empty() if not found


    // UPDATE - Update an address from DTO
    // todo: Implement service method to update an address from DTO, return updated DTO, or Optional.empty() if not found


    // DELETE - Delete an address by ID, return true if found
    // todo: Implement service method to delete an address by ID, return true if found and deleted, false if not found


    // HELPER - Method to map Address entity to DTO
    private ApiRestaurantDTO mapRowToRestaurantDTO(Object[] row) {
        ApiRestaurantDTO dto = new ApiRestaurantDTO();
        dto.setId(((Number) row[0]).intValue());
        dto.setName((String) row[1]);
        dto.setPriceRange(((Number) row[2]).intValue());
        dto.setRating(((Number) row[3]).intValue());
        return dto;
    }
}