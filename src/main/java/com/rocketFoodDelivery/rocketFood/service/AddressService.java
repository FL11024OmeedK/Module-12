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
    // These methods take/return DTOs and delegate to the native SQL repository methods.


    // CREATE - Insert a new address from a DTO and return it with its generated id.
    // @Transactional is REQUIRED: saveAddress() and getLastInsertedId() must run on the SAME
    // database connection, otherwise LAST_INSERT_ID() would not see the row we just inserted.
    @Transactional
    public ApiAddressDTO createAddress(ApiAddressDTO addressDto) {
        addressRepository.saveAddress(
                addressDto.getStreetAddress(),
                addressDto.getCity(),
                addressDto.getPostalCode());

        int newId = addressRepository.getLastInsertedId();
        addressDto.setId(newId);
        return addressDto;
    }


    // READ - Return every address as a DTO.
    public List<ApiAddressDTO> getAllAddressesAsDtos() {
        return addressRepository.findAllAddresses().stream()
                .map(this::mapAddressToDTO)
                .collect(Collectors.toList());
    }


    // READ - Return a single address as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiAddressDTO> getAddressByIdAsDto(int id) {
        return addressRepository.findAddressById(id)
                .map(this::mapAddressToDTO);
    }


    // UPDATE - Update an existing address from a DTO.
    // Returns the updated DTO, or Optional.empty() if no address has the given id.
    @Transactional
    public Optional<ApiAddressDTO> updateAddress(int id, ApiAddressDTO addressDto) {
        if (addressRepository.findAddressById(id).isEmpty()) {
            return Optional.empty();
        }

        addressRepository.updateAddress(
                id,
                addressDto.getStreetAddress(),
                addressDto.getCity(),
                addressDto.getPostalCode());

        addressDto.setId(id);
        return Optional.of(addressDto);
    }


    // DELETE - Delete an address by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteAddress(int id) {
        if (addressRepository.findAddressById(id).isEmpty()) {
            return false;
        }
        addressRepository.deleteAddressById(id);
        return true;
    }


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
