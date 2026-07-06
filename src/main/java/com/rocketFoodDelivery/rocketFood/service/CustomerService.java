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
import com.rocketFoodDelivery.rocketFood.models.Customer;
import com.rocketFoodDelivery.rocketFood.models.User;

// Project DTOs
import com.rocketFoodDelivery.rocketFood.dtos.customer.ApiCustomerDTO;

// Project repositories
import com.rocketFoodDelivery.rocketFood.repository.CustomerRepository;

@Service       
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Constructor
    public CustomerService(CustomerRepository customerRepository){
        this.customerRepository = customerRepository;
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
