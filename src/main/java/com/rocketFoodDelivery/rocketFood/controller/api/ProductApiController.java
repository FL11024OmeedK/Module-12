package com.rocketFoodDelivery.rocketFood.controller.api;

import com.rocketFoodDelivery.rocketFood.dtos.product.ApiCreateProductDTO;
import com.rocketFoodDelivery.rocketFood.dtos.product.ApiProductDTO;
import com.rocketFoodDelivery.rocketFood.exception.ResourceNotFoundException;
import com.rocketFoodDelivery.rocketFood.service.ProductService;
import com.rocketFoodDelivery.rocketFood.util.ResponseBuilder;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductApiController {

    // Service dependency (constructor injection)
    private final ProductService productService;

    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }


    // ==================== DTO-Based API Endpoints ====================


    // GET /api/products - Get all products (optionally filtered by ?restaurant={id})
    @GetMapping("/api/products")
    public ResponseEntity<Object> getAllProducts(@RequestParam(name = "restaurant", required = false) Integer restaurant) {
        if (restaurant != null) {
            return ResponseBuilder.buildOkResponse(productService.getProductsByRestaurantAsDtos(restaurant));
        }
        return ResponseBuilder.buildOkResponse(productService.getAllProductsAsDtos());
    }


    // GET /api/products/{id} - Get product by ID
    @GetMapping("/api/products/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable int id) {
        ApiProductDTO product = productService.getProductByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(product);
    }


    // POST /api/products - Create new product
    @PostMapping("/api/products")
    public ResponseEntity<Object> createProduct(@Valid @RequestBody ApiCreateProductDTO productDto) {
        ApiProductDTO created = productService.createProduct(productDto);
        return ResponseBuilder.buildCreatedResponse(created);
    }


    // PUT /api/products/{id} - Update product by ID
    @PutMapping("/api/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable int id, @Valid @RequestBody ApiCreateProductDTO productDto) {
        ApiProductDTO updated = productService.updateProduct(id, productDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        return ResponseBuilder.buildOkResponse(updated);
    }


    // DELETE /api/products/{id} - Delete product by ID
    @DeleteMapping("/api/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable int id) {
        // Fetch first so we can (a) return the deleted data and (b) 404 if it never existed.
        ApiProductDTO product = productService.getProductByIdAsDto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        productService.deleteProduct(id);
        return ResponseBuilder.buildOkResponse(product);
    }
}
