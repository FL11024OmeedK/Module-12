package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.order.ApiAssignCourierDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiOrderDTO;
import com.rocketFoodDelivery.rocketFood.dtos.order.ApiUpdateRatingDTO;
import com.rocketFoodDelivery.rocketFood.exception.BadRequestException;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.OrderService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class OrderApiController {
    private final OrderService orderService;


    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/orders - Get orders by type and id
    // todo: implement get orders endpoint

    
    // GET /api/orders?type={type}&id={id} - Get orders by type and id
    @GetMapping("/api/orders")
    public ResponseEntity<Object> getOrders(
            @RequestParam(name = "type") String type,
            @RequestParam(name = "id") int id) {
        if (!type.equals("customer") && !type.equals("restaurant") && !type.equals("courier")) {
            throw new BadRequestException("Type must be 'restaurant' or 'customer' or 'courier'");
        }
        List<ApiOrderDTO> orders = orderService.getOrdersByTypeAndId(type, id);
        return ResponseEntity.ok(orders);
    }

    // POST /api/orders - Create new order
    // todo: implement create endpoint


    // PUT /api/orders/{id} - Update order by ID
    // todo: implement update endpoint


    // DELETE /api/orders/{id} - Delete order by ID
    // todo: implement delete endpoint


    // ==================== Custom Endpoints ====================


    // --- Assign a courier to an order ---
    @PutMapping("/api/order/{id}/courier")
    public ResponseEntity<Object> assignCourier(@PathVariable int id, @RequestBody ApiAssignCourierDTO dto) {
        Optional<ApiOrderDTO> updated = orderService.assignCourier(id, dto);
        if (updated.isEmpty()) throw new ResourceNotFoundException(String.format("Order with id %d or courier not found", id));
        return ResponseBuilder.buildOkResponse(updated.get());
    }


    // --- Update an order's rating ---
    @PutMapping("/api/order/{id}/rating")
    public ResponseEntity<Object> updateRating(@PathVariable int id, @RequestBody ApiUpdateRatingDTO dto) {
        Optional<ApiOrderDTO> updated = orderService.updateRating(id, dto);
        if (updated.isEmpty()) throw new ResourceNotFoundException(String.format("Order with id %d not found", id));
        return ResponseBuilder.buildOkResponse(updated.get());
    }
}
