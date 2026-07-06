package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.orderStatus.ApiOrderStatusDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.service.OrderStatusService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;
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
    // todo: implement get all
    

    // GET /api/order-statuses/{id} - Get order status by ID
    // todo: implement get by id endpoint
    

    // POST /api/order-statuses - Create new order status
    // todo: implement create endpoint
    

    // PUT /api/order-statuses/{id} - Update order status by ID
    // todo: implement update endpoint
    

    // DELETE /api/order-statuses/{id} - Delete order status by ID
    // todo: implement delete endpoint


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
