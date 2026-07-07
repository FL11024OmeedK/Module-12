package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.customer.ApiCustomerDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.CustomerService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CustomerApiController {

    // Service dependency (constructor injection)
    private final CustomerService customerService;

    public CustomerApiController(CustomerService customerService) {
        this.customerService = customerService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/customers - Get all customers
    @GetMapping("/api/customers")
    public ResponseEntity<Object> getAllCustomers() {
        return ResponseBuilder.buildOkResponse(customerService.getAllCustomersAsDtos());
    }


    // GET /api/customers/{id} - Get customer by ID
    @GetMapping("/api/customers/{id}")
    public ResponseEntity<Object> getCustomerById(@PathVariable int id) {
        ApiCustomerDTO customer = customerService.getCustomerByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(customer);
    }


    // POST /api/customers - Create new customer
    @PostMapping("/api/customers")
    public ResponseEntity<Object> createCustomer(@Valid @RequestBody ApiCustomerDTO customerDto) {
        ApiCustomerDTO created = customerService.createCustomer(customerDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/customers/{id} - Update customer by ID
    @PutMapping("/api/customers/{id}")
    public ResponseEntity<Object> updateCustomer(@PathVariable int id, @Valid @RequestBody ApiCustomerDTO customerDto) {
        ApiCustomerDTO updated = customerService.updateCustomer(id, customerDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/customers/{id} - Delete customer by ID
    @DeleteMapping("/api/customers/{id}")
    public ResponseEntity<Object> deleteCustomer(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiCustomerDTO customer = customerService.getCustomerByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer with id " + id + " not found"));
        customerService.deleteCustomer(id);
        return ResponseBuilder.buildOkResponse(customer);
    }
}
