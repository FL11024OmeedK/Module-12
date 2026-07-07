package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.courierStatus.ApiCourierStatusDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.CourierStatusService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourierStatusApiController {

    // Service dependency (constructor injection)
    private final CourierStatusService courierStatusService;

    public CourierStatusApiController(CourierStatusService courierStatusService) {
        this.courierStatusService = courierStatusService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/courier-statuses - Get all courier statuses
    @GetMapping("/api/courier-statuses")
    public ResponseEntity<Object> getAllCourierStatuses() {
        return ResponseBuilder.buildOkResponse(courierStatusService.getAllCourierStatusesAsDtos());
    }


    // GET /api/courier-statuses/{id} - Get courier status by ID
    @GetMapping("/api/courier-statuses/{id}")
    public ResponseEntity<Object> getCourierStatusById(@PathVariable int id) {
        ApiCourierStatusDTO status = courierStatusService.getCourierStatusByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier status with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(status);
    }


    // POST /api/courier-statuses - Create new courier status
    @PostMapping("/api/courier-statuses")
    public ResponseEntity<Object> createCourierStatus(@Valid @RequestBody ApiCourierStatusDTO statusDto) {
        ApiCourierStatusDTO created = courierStatusService.createCourierStatus(statusDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/courier-statuses/{id} - Update courier status by ID
    @PutMapping("/api/courier-statuses/{id}")
    public ResponseEntity<Object> updateCourierStatus(@PathVariable int id, @Valid @RequestBody ApiCourierStatusDTO statusDto) {
        ApiCourierStatusDTO updated = courierStatusService.updateCourierStatus(id, statusDto)
                .orElseThrow(() -> new ResourceNotFoundException("Courier status with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/courier-statuses/{id} - Delete courier status by ID
    @DeleteMapping("/api/courier-statuses/{id}")
    public ResponseEntity<Object> deleteCourierStatus(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiCourierStatusDTO status = courierStatusService.getCourierStatusByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Courier status with id " + id + " not found"));
        courierStatusService.deleteCourierStatus(id);
        return ResponseBuilder.buildOkResponse(status);
    }
}
