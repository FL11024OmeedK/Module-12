package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusCrudDTO;
import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.OrderStatusService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderStatusApiController {
    private final OrderStatusService orderStatusService;


    public OrderStatusApiController(OrderStatusService orderStatusService) {
        this.orderStatusService = orderStatusService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/order-statuses - Get all order statuses
    @GetMapping("/api/order-statuses")
    public ResponseEntity<Object> getAllOrderStatuses() {
        return ResponseBuilder.buildOkResponse(orderStatusService.getAllOrderStatusesAsDtos());
    }


    // GET /api/order-statuses/{id} - Get order status by ID
    @GetMapping("/api/order-statuses/{id}")
    public ResponseEntity<Object> getOrderStatusById(@PathVariable int id) {
        ApiOrderStatusCrudDTO status = orderStatusService.getOrderStatusByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order status with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(status);
    }


    // POST /api/order-statuses - Create new order status
    @PostMapping("/api/order-statuses")
    public ResponseEntity<Object> createOrderStatus(@Valid @RequestBody ApiOrderStatusCrudDTO statusDto) {
        ApiOrderStatusCrudDTO created = orderStatusService.createOrderStatus(statusDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/order-statuses/{id} - Update order status by ID
    @PutMapping("/api/order-statuses/{id}")
    public ResponseEntity<Object> updateOrderStatus(@PathVariable int id, @Valid @RequestBody ApiOrderStatusCrudDTO statusDto) {
        ApiOrderStatusCrudDTO updated = orderStatusService.updateOrderStatus(id, statusDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order status with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/order-statuses/{id} - Delete order status by ID
    @DeleteMapping("/api/order-statuses/{id}")
    public ResponseEntity<Object> deleteOrderStatus(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiOrderStatusCrudDTO status = orderStatusService.getOrderStatusByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order status with id " + id + " not found"));
        orderStatusService.deleteOrderStatus(id);
        return ResponseBuilder.buildOkResponse(status);
    }


    // ==================== Custom Endpoints ====================

    // --- Update an order's status ---
    @PostMapping("/api/order/{order_id}/status")
    public ResponseEntity<Object> updateOrderStatus(@PathVariable("order_id") int orderId, @RequestBody ApiOrderStatusDTO statusDTO) {
        if (statusDTO.getStatus() == null || statusDTO.getStatus().isBlank()) {
            throw new BadRequestException("Status is required");
        }
        ApiOrderStatusDTO response = orderStatusService.updateOrderStatusForOrder(orderId, statusDTO.getStatus())
                .orElseThrow(() -> new BadRequestException("Invalid order id or status: " + statusDTO.getStatus()));
        return ResponseBuilder.buildOkResponse(response);
    }
}
