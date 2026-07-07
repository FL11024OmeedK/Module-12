package com.rocketFoodDelivery.rocketFood.service;

// Java standard library
import java.util.List;
import java.util.Optional;

// Spring Framework
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Project models
import com.rocketFoodDelivery.rocketFood.models.Customer;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.customer.ApiCustomerDTO;

// Project exceptions
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.AddressRepository;
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;
import com.rocketFoodDelivery.rocketFood.repository.UserRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    // Constructor injection
    public CustomerService(CustomerRepository customerRepository,
                           UserRepository userRepository,
                           AddressRepository addressRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    // ==================== JPA CRUD Service Methods ====================

    // CREATE / UPDATE - Save entity using JPA
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // READ - Find all customers using JPA
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    // READ - Find a customer by ID using JPA
    public Optional<Customer> findCustomerById(int id) {
        return customerRepository.findById(id);
    }

    // READ - Find a customer by user ID using native SQL
    public Optional<Customer> findCustomerByUserId(int userId) {
        return customerRepository.findCustomerByUserId(userId);
    }

    // DELETE - Delete a customer by ID using JPA
    public void deleteCustomerById(int id) {
        customerRepository.deleteById(id);
    }

    // ==================== DTO-Based Service Methods (used by API controller) ====================


    // CREATE - Create a customer from a DTO and return it with its generated id.
    // @Transactional keeps saveCustomer() and getLastInsertedId() on the same connection.
    @Transactional
    public ApiCustomerDTO createCustomer(ApiCustomerDTO dto) {
        validateReferences(dto);

        // One customer per user (also enforced by the DB unique constraint on user_id).
        if (customerRepository.findCustomerByUserId(dto.getUserId()).isPresent()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " already has a customer");
        }

        customerRepository.saveCustomer(
                dto.getUserId(),
                dto.getAddressId(),
                dto.getPhone(),
                dto.getEmail());

        int newId = customerRepository.getLastInsertedId();
        return customerRepository.findCustomerById(newId)
                .map(this::mapCustomerToDTO)
                .orElseThrow(() -> new BadRequestException("Failed to create customer"));
    }


    // READ - Return every customer as a DTO.
    public List<ApiCustomerDTO> getAllCustomersAsDtos() {
        return customerRepository.findAllCustomers().stream()
                .map(this::mapCustomerToDTO)
                .toList();
    }


    // READ - Return a single customer as a DTO, or Optional.empty() if it does not exist.
    public Optional<ApiCustomerDTO> getCustomerByIdAsDto(int id) {
        return customerRepository.findCustomerById(id)
                .map(this::mapCustomerToDTO);
    }


    // UPDATE - Update an existing customer from a DTO (phone, email, active only).
    // Returns the updated DTO, or Optional.empty() if no customer has the given id.
    @Transactional
    public Optional<ApiCustomerDTO> updateCustomer(int id, ApiCustomerDTO dto) {
        Optional<Customer> existingOpt = customerRepository.findCustomerById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        Customer existing = existingOpt.get();
        // user_id and address_id are immutable on update — carry the stored values into the response.
        int userId = existing.getUser().getId();
        int addressId = existing.getAddress().getId();

        boolean active = dto.getActive() != null ? dto.getActive() : true;
        customerRepository.updateCustomer(id, dto.getPhone(), dto.getEmail(), active);

        ApiCustomerDTO result = new ApiCustomerDTO();
        result.setId(id);
        result.setUserId(userId);
        result.setAddressId(addressId);
        result.setPhone(dto.getPhone());
        result.setEmail(dto.getEmail());
        result.setActive(active);
        return Optional.of(result);
    }


    // DELETE - Delete a customer by id. Returns true if it existed and was deleted, false otherwise.
    @Transactional
    public boolean deleteCustomer(int id) {
        if (customerRepository.findCustomerById(id).isEmpty()) {
            return false;
        }
        customerRepository.deleteCustomerById(id);
        return true;
    }


    // HELPER - Validate that the referenced user and address exist.
    private void validateReferences(ApiCustomerDTO dto) {
        if (userRepository.findById(dto.getUserId()).isEmpty()) {
            throw new BadRequestException("User with id " + dto.getUserId() + " not found");
        }
        if (addressRepository.findById(dto.getAddressId()).isEmpty()) {
            throw new BadRequestException("Address with id " + dto.getAddressId() + " not found");
        }
    }


    // HELPER - Method to map Customer entity to DTO
    private ApiCustomerDTO mapCustomerToDTO(Customer customer) {
        ApiCustomerDTO dto = new ApiCustomerDTO();
        dto.setId(customer.getId());
        dto.setUserId(customer.getUser() != null ? customer.getUser().getId() : 0);
        dto.setAddressId(customer.getAddress() != null ? customer.getAddress().getId() : 0);
        dto.setPhone(customer.getPhone());
        dto.setEmail(customer.getEmail());
        dto.setActive(customer.getActive());
        return dto;
    }
}
