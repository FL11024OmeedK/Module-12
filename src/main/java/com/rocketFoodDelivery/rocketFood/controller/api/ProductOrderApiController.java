package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.productOrder.ApiProductOrderDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.ProductOrderService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductOrderApiController {

    // Service dependency (constructor injection)
    private final ProductOrderService productOrderService;

    public ProductOrderApiController(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/product-orders - Get all product orders
    @GetMapping("/api/product-orders")
    public ResponseEntity<Object> getAllProductOrders() {
        return ResponseBuilder.buildOkResponse(productOrderService.getAllProductOrdersAsDtos());
    }


    // GET /api/product-orders/{id} - Get product order by ID
    @GetMapping("/api/product-orders/{id}")
    public ResponseEntity<Object> getProductOrderById(@PathVariable int id) {
        ApiProductOrderDTO productOrder = productOrderService.getProductOrderByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product order with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(productOrder);
    }


    // POST /api/product-orders - Create new product order
    @PostMapping("/api/product-orders")
    public ResponseEntity<Object> createProductOrder(@Valid @RequestBody ApiProductOrderDTO productOrderDto) {
        ApiProductOrderDTO created = productOrderService.createProductOrderFromDto(productOrderDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/product-orders/{id} - Update product order by ID
    @PutMapping("/api/product-orders/{id}")
    public ResponseEntity<Object> updateProductOrder(@PathVariable int id, @Valid @RequestBody ApiProductOrderDTO productOrderDto) {
        ApiProductOrderDTO updated = productOrderService.updateProductOrderFromDto(id, productOrderDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product order with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/product-orders/{id} - Delete product order by ID
    @DeleteMapping("/api/product-orders/{id}")
    public ResponseEntity<Object> deleteProductOrder(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiProductOrderDTO productOrder = productOrderService.getProductOrderByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product order with id " + id + " not found"));
        productOrderService.deleteProductOrder(id);
        return ResponseBuilder.buildOkResponse(productOrder);
    }
}
