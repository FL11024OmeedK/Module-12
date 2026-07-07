package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.courier.ApiCourierDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.CourierService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourierApiController {

    // Service dependency (constructor injection)
    private final CourierService courierService;

    public CourierApiController(CourierService courierService) {
        this.courierService = courierService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/couriers - Get all couriers
    @GetMapping("/api/couriers")
    public ResponseEntity<Object> getAllCouriers() {
        return ResponseBuilder.buildOkResponse(courierService.getAllCouriersAsDtos());
    }


    // GET /api/couriers/{id} - Get courier by ID
    @GetMapping("/api/couriers/{id}")
    public ResponseEntity<Object> getCourierById(@PathVariable int id) {
        ApiCourierDTO courier = courierService.getCourierByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(courier);
    }


    // POST /api/couriers - Create new courier
    @PostMapping("/api/couriers")
    public ResponseEntity<Object> createCourier(@Valid @RequestBody ApiCourierDTO courierDto) {
        ApiCourierDTO created = courierService.createCourier(courierDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/couriers/{id} - Update courier by ID
    @PutMapping("/api/couriers/{id}")
    public ResponseEntity<Object> updateCourier(@PathVariable int id, @Valid @RequestBody ApiCourierDTO courierDto) {
        ApiCourierDTO updated = courierService.updateCourier(id, courierDto)
                .orElseThrow(() -> new ResourceNotFoundException("Courier with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/couriers/{id} - Delete courier by ID
    @DeleteMapping("/api/couriers/{id}")
    public ResponseEntity<Object> deleteCourier(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiCourierDTO courier = courierService.getCourierByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier with id " + id + " not found"));
        courierService.deleteCourier(id);
        return ResponseBuilder.buildOkResponse(courier);
    }
}
