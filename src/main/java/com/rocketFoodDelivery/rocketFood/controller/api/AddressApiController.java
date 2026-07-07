package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.address.ApiAddressDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.AddressService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AddressApiController {

    // Service dependency (constructor injection)
    private final AddressService addressService;

    public AddressApiController(AddressService addressService) {
        this.addressService = addressService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/addresses - Get all addresses
    @GetMapping("/api/addresses")
    public ResponseEntity<Object> getAllAddresses() {
        return ResponseBuilder.buildOkResponse(addressService.getAllAddressesAsDtos());
    }


    // GET /api/addresses/{id} - Get address by ID
    @GetMapping("/api/addresses/{id}")
    public ResponseEntity<Object> getAddressById(@PathVariable int id) {
        ApiAddressDTO address = addressService.getAddressByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(address);
    }


    // POST /api/addresses - Create new address
    @PostMapping("/api/addresses")
    public ResponseEntity<Object> createAddress(@Valid @RequestBody ApiAddressDTO addressDto) {
        ApiAddressDTO created = addressService.createAddress(addressDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/addresses/{id} - Update address by ID
    @PutMapping("/api/addresses/{id}")
    public ResponseEntity<Object> updateAddress(@PathVariable int id, @Valid @RequestBody ApiAddressDTO addressDto) {
        ApiAddressDTO updated = addressService.updateAddress(id, addressDto)
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/addresses/{id} - Delete address by ID
    @DeleteMapping("/api/addresses/{id}")
    public ResponseEntity<Object> deleteAddress(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiAddressDTO address = addressService.getAddressByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address with id " + id + " not found"));
        addressService.deleteAddress(id);
        return ResponseBuilder.buildOkResponse(address);
    }
}
