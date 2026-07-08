package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Address;
import com.rocketFoodDelivery.rocketFood.models.Restaurant;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiCreateRestaurantDTO;
import com.rocketFoodDelivery.rocketFood.dtos.restaurant.ApiRestaurantDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

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


    // CREATE - Create a restaurant (and its address) from a DTO, return the detailed DTO.
    // @Transactional keeps the address insert, saveRestaurant(), and getLastInsertedId() on one connection.
    @Transactional
    public ApiRestaurantDTO createRestaurant(ApiCreateRestaurantDTO dto) {
        if (userRepository.findById(dto.getUserId()).isEmpty()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " not found");
        }
        if (dto.getAddress() == null) {
            throw new BadRequestException("Address is required");
        }

        // Persist the restaurant's address first (JPA) so we have its generated id.
        ApiAddressDTO addressDto = dto.getAddress();
        Address address = addressRepository.save(Address.builder()
                .streetAddress(addressDto.getStreetAddress())
                .city(addressDto.getCity())
                .postalCode(addressDto.getPostalCode())
                .build());

        restaurantRepository.saveRestaurant(
                dto.getUserId(),
                address.getId(),
                dto.getName(),
                dto.getPriceRange(),
                dto.getPhone(),
                dto.getEmail());
        int newId = restaurantRepository.getLastInsertedId();

        return buildDetailedDTO(newId, dto.getName(), dto.getPriceRange(), dto.getPhone(),
                dto.getEmail(), dto.getUserId(), address);
    }


    // READ - Return restaurants as summary DTOs (optionally filtered by rating and/or price range).
    public List<ApiRestaurantDTO> getRestaurantsAsDtos(Integer rating, Integer priceRange) {
        return restaurantRepository.findRestaurantsByRatingAndPriceRange(rating, priceRange).stream()
                .map(this::mapRowToRestaurantDTO)
                .toList();
    }


    // READ - Return a single restaurant as a summary DTO (with rating), or empty if it does not exist.
    public Optional<ApiRestaurantDTO> getRestaurantByIdAsDto(int id) {
        List<Object[]> rows = restaurantRepository.findRestaurantWithAverageRatingById(id);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(mapRowToRestaurantDTO(rows.get(0)));
    }


    // UPDATE - Update a restaurant's name/price_range/phone from a DTO, return the detailed DTO.
    // Returns Optional.empty() if no restaurant has the given id (-> 404).
    @Transactional
    public Optional<ApiRestaurantDTO> updateRestaurant(int id, ApiCreateRestaurantDTO dto) {
        Optional<Restaurant> existingOpt = restaurantRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Restaurant existing = existingOpt.get();
        // user_id, email and address are immutable on update — read them from storage for the response.
        int userId = existing.getUser() != null ? existing.getUser().getId() : 0;
        String email = existing.getEmail();
        Address address = existing.getAddress();

        restaurantRepository.updateRestaurant(id, dto.getName(), dto.getPriceRange(), dto.getPhone());

        return Optional.of(buildDetailedDTO(id, dto.getName(), dto.getPriceRange(), dto.getPhone(),
                email, userId, address));
    }


    // DELETE - Delete a restaurant by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteRestaurant(int id) {
        if (restaurantRepository.findById(id).isEmpty()) {
            return false;
        }
        restaurantRepository.deleteRestaurantById(id);
        return true;
    }


    // HELPER - Build the detailed restaurant DTO (create/update response shape).
    private ApiRestaurantDTO buildDetailedDTO(int id, String name, int priceRange, String phone,
                                              String email, int userId, Address address) {
        ApiRestaurantDTO dto = new ApiRestaurantDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPriceRange(priceRange);
        dto.setPhone(phone);
        dto.setEmail(email);
        dto.setUserId(userId);
        if (address != null) {
            ApiAddressDTO addressDto = new ApiAddressDTO();
            addressDto.setId(address.getId());
            addressDto.setStreetAddress(address.getStreetAddress());
            addressDto.setCity(address.getCity());
            addressDto.setPostalCode(address.getPostalCode());
            dto.setAddress(addressDto);
        }
        return dto;
    }


    // HELPER - Method to map an Object[] row (id, name, price_range, rating) to a summary DTO
    private ApiRestaurantDTO mapRowToRestaurantDTO(Object[] row) {
        ApiRestaurantDTO dto = new ApiRestaurantDTO();
        dto.setId(((Number) row[0]).intValue());
        dto.setName((String) row[1]);
        dto.setPriceRange(((Number) row[2]).intValue());
        dto.setRating(((Number) row[3]).intValue());
        return dto;
    }
}
