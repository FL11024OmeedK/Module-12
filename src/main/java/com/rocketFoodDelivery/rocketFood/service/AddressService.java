package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Spring Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Address;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.RestaurantRepository;

@Service       
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    // Constructor
    public AddressService(AddressRepository addressRepository, RestaurantRepository restaurantRepository){
        this.addressRepository = addressRepository;
        this.restaurantRepository = restaurantRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    // READ - Find all addresses using JPA
    public List<Address> findAllAddresses() {
        return addressRepository.findAll();
    }

    // READ - Find an address by ID using JPA
    public Optional<Address> findAddressById(int id) {
        return addressRepository.findById(id);
    }

    // DELETE - Delete an address by ID using JPA
    public void deleteAddressById(int id) {
        addressRepository.deleteById(id);
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
    private ApiAddressDTO mapAddressToDTO(Address address) {
        ApiAddressDTO dto = new ApiAddressDTO();
        dto.setId(address.getId());
        dto.setStreetAddress(address.getStreetAddress());
        dto.setCity(address.getCity());
        dto.setPostalCode(address.getPostalCode());
        return dto;
    }


    // ==================== Custom Business Logic Methods ====================

    // Find addresses not assigned to any restaurant
    public List<Address> findAvailableAddresses(Integer currentRestaurantId) {
        List<Address> allAddresses = addressRepository.findAll();
        Set<Integer> usedAddressIds = restaurantRepository.findAll().stream()
            .filter(restaurant -> restaurant.getAddress() != null)
            .filter(restaurant -> currentRestaurantId == null || restaurant.getId() != currentRestaurantId)
            .map(restaurant -> restaurant.getAddress().getId())
            .collect(Collectors.toSet());
        return allAddresses.stream()
            .filter(address -> !usedAddressIds.contains(address.getId()))
            .toList();
    }
}